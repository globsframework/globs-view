package org.globsframework.view.filter.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;
import org.globsframework.view.DateUtils;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NotEqualType {
    static private final Logger LOGGER = LoggerFactory.getLogger(NotEqualType.class);
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    public static final StringField value;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("NotEqual");
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

                        if (field instanceof DateTimeField) {
                            ZonedDateTime compareTo = DateUtils.parse(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((DateTimeField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(zonedDateTime -> !compareTo.isEqual(zonedDateTime));
                        }
                        if (field instanceof DateField) {
                            LocalDate compareTo = LocalDate.parse(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((DateField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> !compareTo.isEqual(value));
                        }
                        if (field instanceof IntegerField) {
                            int compareTo = Integer.parseInt(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((IntegerField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value != compareTo);
                        }
                        if (field instanceof LongField) {
                            long compareTo = Long.parseLong(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((LongField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value != compareTo);
                        }
                        if (field instanceof DoubleField) {
                            double compareTo = Double.parseDouble(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((DoubleField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value != compareTo);
                        }
                        if (field instanceof StringField) {
                            String compareTo = filter.get(value);
                            return glob -> jump.from(glob)
                                    .map(((StringField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> !compareTo.equals(value));
                        }
                        if (field instanceof BooleanField) {
                            boolean compareTo = Boolean.parseBoolean(filter.get(value));
                            return glob -> jump.from(glob).map(((BooleanField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(Predicate.not(Predicate.isEqual(compareTo)));
                        }

                        String msg = "Field " + field.getFullName() + " of type " + field.getValueClass() + " not managed";
                        LOGGER.error(msg);
                        throw new RuntimeException(msg);
                    }
                });
        TYPE = typeBuilder.build();
    }
}
