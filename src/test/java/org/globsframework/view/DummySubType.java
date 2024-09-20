package org.globsframework.view;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class DummySubType {
    public static GlobType TYPE;

    public static StringField s1;

    public static StringField s2;

    static {
        GlobTypeLoaderFactory.create(DummySubType.class).load();
    }
}
