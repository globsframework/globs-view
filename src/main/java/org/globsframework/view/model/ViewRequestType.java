package org.globsframework.view.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.view.filter.model.FilterType;

public class ViewRequestType {
    public static GlobType TYPE;

    @Target(ViewBreakdown.class)
    public static GlobArrayField breakdowns;

    @Target(ViewOutput.class)
    public static GlobArrayField output;

    @Target(FilterType.class)
    public static GlobField filter;

    static {
        GlobTypeLoaderFactory.create(ViewRequestType.class).load();
    }

}
