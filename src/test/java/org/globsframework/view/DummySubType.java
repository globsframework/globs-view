package org.globsframework.view;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class DummySubType {
    public static GlobType TYPE;

    public static StringField s1;

    public static StringField s2;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("DummySubType");
        s1 = builder.declareStringField("s1");
        s2 = builder.declareStringField("s2");
        TYPE = builder.build();
    }
}
