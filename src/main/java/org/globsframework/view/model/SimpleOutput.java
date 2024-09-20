package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class SimpleOutput {
    public static GlobType TYPE;

    public static StringField uniqueName;

    public static StringField alias;

    public static StringField outputName;

    public static StringField nativeType;

    static {
        GlobTypeLoaderFactory.create(SimpleOutput.class).load();
    }
}
