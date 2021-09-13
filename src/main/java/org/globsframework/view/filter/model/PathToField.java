package org.globsframework.view.filter.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.model.Glob;
import org.globsframework.view.model.SimpleBreakdown;

import java.util.Map;
import java.util.stream.Stream;

public class PathToField {
    private Glob filter;
    private GlobType rootType;
    private Map<String, Glob> dico;
    private String uniqueName;
    private Jump jump;
    private Field field;

    public PathToField(String uniqueName, GlobType rootType, Map<String, Glob> dico) {
        this.rootType = rootType;
        this.dico = dico;
        this.uniqueName = uniqueName;
    }

    public Jump getJump() {
        return jump;
    }

    public Field getField() {
        return field;
    }

    public PathToField invoke() {
        Glob breakdown = dico.get(uniqueName);
        String[] strings = breakdown.get(SimpleBreakdown.path);
        GlobType lastType = rootType;
        if (strings.length != 0) {
            Jump[] jumps = new Jump[strings.length];
            for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
                String string = strings[i];
                Field field = lastType.getField(string);
                if (field instanceof GlobField) {
                    jumps[i] = new Jump() {
                        public Stream<Glob> from(Glob glob) {
                            Glob ch = glob.get(((GlobField) field));
                            return ch == null ? Stream.empty() : Stream.of(ch);
                        }
                    };
                    lastType = ((GlobField) field).getTargetType();
                } else if (field instanceof GlobArrayField) {
                    jumps[i] = new Jump() {
                        public Stream<Glob> from(Glob glob) {
                            Glob[] ch = glob.get(((GlobArrayField) field));
                            return ch == null || ch.length == 0 ? Stream.empty() : Stream.of(ch);
                        }
                    };
                    lastType = ((GlobArrayField) field).getTargetType();
                } else {
                    throw new RuntimeException("Navigation in union type no developed " + field.getFullName());
                }
                this.field = field;
            }
            jump = glob -> {
                Stream<Glob> current = Stream.of(glob);
                for (Jump jp : jumps) {
                    current = current.flatMap(jp::from);
                }
                return current;
            };
        } else {
            jump = Stream::of;
        }
        field = lastType.getField(breakdown.get(SimpleBreakdown.fieldName));
        return this;
    }
}
