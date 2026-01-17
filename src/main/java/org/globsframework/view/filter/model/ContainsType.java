package org.globsframework.view.filter.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

public class ContainsType {
    static private final Logger LOGGER = LoggerFactory.getLogger(ContainsType.class);
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    public static final StringField value;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("Contains");
        uniqueName = typeBuilder.declareStringField("uniqueName");
        value = typeBuilder.declareStringField("value");
        typeBuilder.register(WantedField.class, new WantedField() {
                    public void wanted(Glob filter, Consumer<String> wantedUniqueName) {
                        wantedUniqueName.accept(filter.get(uniqueName));
                    }
                })
                .register(Rewrite.class, glob -> glob)
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico, boolean fullQuery) {
                        PathToField pathToField = new PathToField(filter.get(uniqueName), rootType, dico).invoke();
                        Jump jump = pathToField.getJump();
                        Field field = pathToField.getField();
                        if (field instanceof StringField) {
                            String compareTo = filter.get(value);
                            return glob -> jump.from(glob)
                                    .map(((StringField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(s -> s.contains(compareTo));
                        }
                        String msg = "Field " + field.getFullName() + " of type " + field.getValueClass() + " not managed";
                        LOGGER.error(msg);
                        throw new RuntimeException(msg);
                    }
                });
        TYPE = typeBuilder.build();
    }
}
