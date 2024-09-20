package org.globsframework.view.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Comment_;
import org.globsframework.core.metamodel.fields.StringField;

public class ViewOutput {
    public static GlobType TYPE;

    public static StringField uniqueName;

    @Comment_("name in the result")
    public static StringField name;

    static {
        GlobTypeLoaderFactory.create(ViewOutput.class).load();
    }
}
