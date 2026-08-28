package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.GlobArrayField;

public class DictionaryType {
    public static final GlobType TYPE;

    public static final GlobArrayField<SimpleBreakdown> breakdowns;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("Dictionary");
        breakdowns = typeBuilder.declareGlobArrayField("breakdowns", () -> SimpleBreakdown.TYPE);
        TYPE = typeBuilder.build();
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
