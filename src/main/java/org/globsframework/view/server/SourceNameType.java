package org.globsframework.view.server;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.KeyField;
import org.globsframework.metamodel.fields.StringArrayField;
import org.globsframework.metamodel.fields.StringField;

public class SourceNameType {
    public static GlobType TYPE;

    @KeyField
    public static StringField ID;

    public static StringArrayField NAME;

    static {
        GlobTypeLoaderFactory.create(SourceNameType.class).load();
    }
}
