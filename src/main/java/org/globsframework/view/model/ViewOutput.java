package org.globsframework.view.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Comment_;
import org.globsframework.metamodel.fields.StringField;

public class ViewOutput {
    public static GlobType TYPE;

    public static StringField uniqueName;

    @Comment_("name in the result")
    public static StringField name;

    static {
        GlobTypeLoaderFactory.create(ViewOutput.class).load();
    }
}
