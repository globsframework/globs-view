package org.globsframework.view.server;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.KeyField;
import org.globsframework.core.metamodel.annotations.KeyField_;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;

public class SourceNameType {
    public static final GlobType TYPE;

    @KeyField_
    public static final StringField ID;

    public static final StringArrayField NAME;

    public static Glob create(String id, String... name) {
        return TYPE.instantiate()
                .set(ID, id)
                .set(NAME, name);
    }

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("SourceNameType");
        TYPE = typeBuilder.unCompleteType();
        ID = typeBuilder.declareStringField("id", KeyField.ZERO);
        NAME = typeBuilder.declareStringArrayField("name");
        typeBuilder.complete();
//        GlobTypeLoaderFactory.create(SourceNameType.class).load();
    }
}
