package org.globsframework.view.filter.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

public class LessOrEqualType {
    static private final Logger LOGGER = LoggerFactory.getLogger(LessOrEqualType.class);
    public static GlobType TYPE;

    public static StringField uniqueName;

    public static StringField value;

    static {
        GlobTypeLoaderFactory.create(LessOrEqualType.class)
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, Map<String, Glob> dico) {
                        PathToField pathToField = new PathToField(filter.get(uniqueName), rootType, dico).invoke();
                        Jump jump = pathToField.getJump();
                        Field field = pathToField.getField();

                        if (field instanceof DateTimeField) {
                            ZonedDateTime compareTo = ZonedDateTime.parse(filter.get(value));
                            return glob -> !compareTo.isBefore(jump.from(glob).get(((DateTimeField) field)));
                        }
                        if (field instanceof DateField) {
                            LocalDate compareTo = LocalDate.parse(filter.get(value));
                            return glob -> !compareTo.isBefore(jump.from(glob).get(((DateField) field)));
                        }
                        if (field instanceof IntegerField) {
                            int compareTo = Integer.parseInt(filter.get(value));
                            return glob -> {
                                Integer value = jump.from(glob).get(((IntegerField) field));
                                if (value == null) {
                                    return false;
                                }
                                return value <= compareTo;
                            };
                        }
                        if (field instanceof LongField) {
                            long compareTo = Long.parseLong(filter.get(value));
                            return glob -> {
                                Long value = jump.from(glob).get(((LongField) field));
                                if (value == null) {
                                    return false;
                                }
                                return value <= compareTo;
                            };
                        }
                        if (field instanceof DoubleField) {
                            double compareTo = Double.parseDouble(filter.get(value));
                            return glob -> {
                                Double value = jump.from(glob).get(((DoubleField) field));
                                if (value == null) {
                                    return false;
                                }
                                return value <= compareTo;
                            };
                        }
                        String msg = "Field " + field.getFullName() + " of type " + field.getValueClass() + " not managed";
                        LOGGER.error(msg);
                        throw new RuntimeException(msg);
                    }
                }).load();
    }
}
