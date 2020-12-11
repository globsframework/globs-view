package org.globsframework.view.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.fields.StringField;

public class ViewBreakdown {
    public static GlobType TYPE;

    public static StringField uniqueName;

    public static StringField aliasName;

    static {
        GlobTypeLoaderFactory.create(ViewBreakdown.class).load();
    }
}
