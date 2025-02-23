package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Comment_;
import org.globsframework.core.metamodel.fields.StringField;

public class ViewOutput {
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    @Comment_("name in the result")
    public static final StringField name;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("ViewOutput");
        TYPE = typeBuilder.unCompleteType();
        uniqueName = typeBuilder.declareStringField("uniqueName");
        name = typeBuilder.declareStringField("name");
        typeBuilder.complete();
//        GlobTypeLoaderFactory.create(ViewOutput.class).load();
    }
}
