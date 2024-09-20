package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobArrayField;

public class DictionaryType {
    public static GlobType TYPE;

    @Target(SimpleBreakdown.class)
    public static GlobArrayField breakdowns;

    static {
        GlobTypeLoaderFactory.create(DictionaryType.class).load();
    }

    /*

    //


    {
      "breakdowns": [{
        "typeName": "toto",
        "fieldName": }]
    }
     */
}
