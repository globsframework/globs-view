package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.view.filter.model.FilterType;

public class ViewRequestType {
    public static final GlobType TYPE;

    public static final GlobArrayField<ViewBreakdown> breakdowns;

    public static final GlobArrayField<ViewOutput> output;

    public static final GlobField<FilterType> filter;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("ViewRequest");
        breakdowns = typeBuilder.declareGlobArrayField("breakdowns", () -> ViewBreakdown.TYPE);
        output = typeBuilder.declareGlobArrayField("output", () -> ViewOutput.TYPE);
        filter = typeBuilder.declareGlobField("filter", () -> FilterType.TYPE);
        TYPE = typeBuilder.build();
    }

}
