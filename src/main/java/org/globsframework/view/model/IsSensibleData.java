package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueGlob;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;

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
