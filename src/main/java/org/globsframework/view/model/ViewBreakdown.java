package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.fields.StringField;

public class ViewBreakdown {
    public static GlobType TYPE;

    public static StringField uniqueName;

    public static StringField aliasName;

    static {
        GlobTypeLoaderFactory.create(ViewBreakdown.class).load();
    }
}
