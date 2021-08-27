package org.globsframework.view.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.metamodel.annotations.InitUniqueGlob;
import org.globsframework.metamodel.annotations.InitUniqueKey;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;

public class IsSensibleData {
    public static GlobType TYPE;

    @InitUniqueKey
    public static Key key;

    @InitUniqueGlob
    public static Glob data;

    static {
        GlobTypeLoaderFactory.create(IsSensibleData.class)
                .register(GlobCreateFromAnnotation.class, annotation -> data
                ).load();

    }
}
