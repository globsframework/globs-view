package org.globsframework.view.filter.model;

import org.globsframework.metamodel.fields.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class IsNullType {
    static private final Logger LOGGER = LoggerFactory.getLogger(IsNullType.class);
    public static GlobType TYPE;

    public static StringField uniqueName;

    static {
        GlobTypeLoaderFactory.create(IsNullType.class)
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
                        return glob -> jump.from(glob)
                                .anyMatch(g -> g.getValue(field) == null);
                    }
                }).load();
    }
}
