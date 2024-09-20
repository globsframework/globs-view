package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
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
