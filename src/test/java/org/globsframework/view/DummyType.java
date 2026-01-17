package org.globsframework.view;

import com.google.gson.Gson;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.GlobTypeResolver;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.json.GlobsGson;

public class DummyType {
    public static GlobType TYPE;

    public static StringField n1;

    public static StringField n2;

    @Target(DummySubType.class)
    public static GlobArrayField subType;

    static {
        GlobTypeBuilder builder = GlobTypeBuilderFactory.create("DummyType");
        n1 = builder.declareStringField("n1");
        n2 = builder.declareStringField("n2");
        subType = builder.declareGlobArrayField("subType", () -> DummySubType.TYPE);
        TYPE = builder.build();
    }

    // pour générer un premier sample.

    public static void main(String[] args) {
        Gson gson = GlobsGson.create(GlobTypeResolver.ERROR);
        System.out.println("DummyType.main " + gson.toJson(new GlobType[]{DummyType.TYPE, DummySubType.TYPE}));

        MutableGlob d1 = DummyType.TYPE.instantiate()
                .set(DummyType.n1, "n1_1")
                .set(DummyType.n2, "n2_1")
                .set(DummyType.subType, new Glob[]{
                        DummySubType.TYPE.instantiate()
                                .set(DummySubType.s1, "s1_1")
                                .set(DummySubType.s2, "s2_1"),
                        DummySubType.TYPE.instantiate()
                                .set(DummySubType.s1, "s1_2")
                                .set(DummySubType.s2, "s2_2"),
                });

        System.out.println("Globs: " + gson.toJson(new Glob[]{d1}));
    }
}
