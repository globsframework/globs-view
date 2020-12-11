package org.globsframework.view.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;

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
