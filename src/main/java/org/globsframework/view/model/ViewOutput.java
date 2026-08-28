package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Comment;
import org.globsframework.core.metamodel.fields.StringField;

public class ViewOutput {
    public static final GlobType TYPE;

    public static final StringField uniqueName;

    public static final StringField name;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("ViewOutput");
        uniqueName = typeBuilder.declareStringField("uniqueName");
        name = typeBuilder.declareStringField("name", Comment.create("name in the result"));
        TYPE = typeBuilder.build();
    }
}
