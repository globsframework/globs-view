package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class SimpleOutput {
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    public static final StringField alias;

    public static final StringField outputName;

    public static final StringField nativeType;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("SimpleOutput");
        TYPE = typeBuilder.unCompleteType();
        uniqueName = typeBuilder.declareStringField("uniqueName");
        alias = typeBuilder.declareStringField("alias");
        outputName = typeBuilder.declareStringField("outputName");
        nativeType = typeBuilder.declareStringField("nativeType");
        typeBuilder.complete();
//        GlobTypeLoaderFactory.create(SimpleOutput.class).load();
    }
}
