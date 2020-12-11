package org.globsframework.view.filter.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.model.Glob;
import org.globsframework.view.model.SimpleBreakdown;

import java.util.Map;

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
                field = lastType.getField(string);
                if (field instanceof GlobField) {
                    jumps[i] = new Jump() {
                        public Glob from(Glob glob) {
                            return glob.get(((GlobField) field));
                        }
                    };
                    lastType = ((GlobField) field).getType();
                } else {
                    throw new RuntimeException("Navigation in array type no developed " + field.getFullName());
                }
            }
            jump = glob -> {
                for (Jump jump1 : jumps) {
                    glob = jump1.from(glob);
                }
                return glob;
            };
        } else {
            jump = glob -> glob;
        }
        field = lastType.getField(breakdown.get(SimpleBreakdown.fieldName));
        return this;
    }
}
