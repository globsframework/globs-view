package org.globsframework.view.server;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.KeyField;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;

public class SourceNameType {
    public static GlobType TYPE;

    @KeyField
    public static StringField ID;

    public static StringArrayField NAME;

    public static Glob create(String id, String... name) {
        return TYPE.instantiate()
                .set(ID, id)
                .set(NAME, name);
    }

    static {
        GlobTypeLoaderFactory.create(SourceNameType.class).load();
    }
}
