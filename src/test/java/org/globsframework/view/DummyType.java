package org.globsframework.view;

import com.google.gson.Gson;
import org.globsframework.metamodel.GlobTypeResolver;
import org.globsframework.json.GlobsGson;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.MutableGlob;

public class DummyType {
    public static GlobType TYPE;

    public static StringField n1;

    public static StringField n2;

    @Target(DummySubType.class)
    public static GlobArrayField subType;

    static {
        GlobTypeLoaderFactory.create(DummyType.class).load();
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
