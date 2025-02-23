package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;

public class SimpleBreakdown {
    public static final GlobType TYPE;

    public static final StringArrayField path; // list of fieldName until

    public static final StringField uniqueName;

    public static final StringField aliasName;

    public static final StringField typeName;

    public static final StringField outputTypeName;

    public static final StringField fieldName;

    public static final StringField nativeType;

    public static final BooleanField isSensibleData;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("SimpleBreakdown");
        TYPE = typeBuilder.unCompleteType();
        path = typeBuilder.declareStringArrayField("path");
        uniqueName = typeBuilder.declareStringField("uniqueName");
        aliasName = typeBuilder.declareStringField("aliasName");
        typeName = typeBuilder.declareStringField("typeName");
        outputTypeName = typeBuilder.declareStringField("outputTypeName");
        fieldName = typeBuilder.declareStringField("fieldName");
        nativeType = typeBuilder.declareStringField("nativeType");
        isSensibleData = typeBuilder.declareBooleanField("isSensibleData");
        typeBuilder.complete();
//        GlobTypeLoaderFactory.create(SimpleBreakdown.class).load();
    }
}
