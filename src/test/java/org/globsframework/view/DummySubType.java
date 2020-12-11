package org.globsframework.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.StringField;

public class DummySubType {
    public static GlobType TYPE;

    public static StringField s1;

    public static StringField s2;

    static {
        GlobTypeLoaderFactory.create(DummySubType.class).load();
    }
}
