package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class ViewBreakdown {
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    public static final StringField aliasName;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("ViewBreakdown");
        uniqueName = typeBuilder.declareStringField("uniqueName");
        aliasName = typeBuilder.declareStringField("aliasName");
        TYPE = typeBuilder.build();
    }
}
