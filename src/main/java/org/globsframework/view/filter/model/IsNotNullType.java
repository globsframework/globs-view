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

import java.util.function.Consumer;

public class IsNotNullType {
    static private final Logger LOGGER = LoggerFactory.getLogger(IsNotNullType.class);
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("IsNotNull");
        TYPE = typeBuilder.unCompleteType();
        uniqueName = typeBuilder.declareStringField("uniqueName");
        typeBuilder.complete();
//        GlobTypeLoaderFactory.create(IsNotNullType.class)
        typeBuilder
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
                                .anyMatch(g -> g.getValue(field) != null);
                    }
                });
    }
}
