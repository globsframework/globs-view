package org.globsframework.view.filter.model;

import org.globsframework.metamodel.fields.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.view.DateUtils;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;
import org.globsframework.view.model.StringAsDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Consumer;

public class StrictlyGreaterType {
    static private final Logger LOGGER = LoggerFactory.getLogger(StrictlyGreaterType.class);

    public static GlobType TYPE;

    public static StringField uniqueName;

    public static StringField value;

    static {
        GlobTypeLoaderFactory.create(StrictlyGreaterType.class)
                .register(WantedField.class, new WantedField() {
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
                                    .anyMatch(d -> d.isAfter(compareTo));
                        }
                        if (field instanceof DateField) {
                            LocalDate compareTo = LocalDate.parse(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((DateField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(d -> d.isAfter(compareTo));
                        }
                        if (field instanceof IntegerField) {
                            int compareTo = Integer.parseInt(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((IntegerField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value > compareTo);
                        }
                        if (field instanceof LongField) {
                            long compareTo = Long.parseLong(filter.get(value));
                            return glob -> jump.from(glob)
                                    .map(((LongField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value > compareTo);
                        }
                        if (field instanceof DoubleField) {
                            double compareTo = Double.parseDouble(filter.get(value));
                            DoubleField dblField = (DoubleField) field;
                            return glob -> jump.from(glob)
                                    .map(dblField)
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value > compareTo);
                        }
                        if (field instanceof StringField) {
                            String compareTo = filter.get(value);
                            if (field.hasAnnotation(StringAsDouble.key)) {
                                double dbl = Double.parseDouble(filter.get(value));
                                StringField dblField = (StringField) field;
                                return glob -> jump.from(glob)
                                        .map(dblField)
                                        .map(StrictlyGreaterType::parseDouble)
                                        .filter(Objects::nonNull)
                                        .anyMatch(value -> value > dbl);

                            }
                            return glob -> jump.from(glob)
                                    .map(((StringField) field))
                                    .filter(Objects::nonNull)
                                    .anyMatch(value -> value.compareToIgnoreCase(compareTo) > 0);
                        }
                        String msg = "Field " + field.getFullName() + " of type " + field.getValueClass() + " not managed";
                        LOGGER.error(msg);
                        throw new RuntimeException(msg);
                    }
                }).load();
    }

    public static Double parseDouble(String dbl) {
        try {
            return dbl == null || dbl.length() == 0 ? null : Double.parseDouble(dbl);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
