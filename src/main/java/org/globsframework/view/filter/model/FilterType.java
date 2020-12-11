package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.GlobUnionField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;

import java.util.Map;

public class FilterType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            GreaterOrEqualType.class, StrictlyGreaterType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class})
    public static GlobUnionField filter;

    static {
        GlobTypeLoaderFactory.create(FilterType.class)
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob globFilter, GlobType rootType, Map<String, Glob> dico) {
                        Glob glob = globFilter.get(filter);
                        return glob.getType().getRegistered(FilterBuilder.class)
                                .create(glob, rootType, dico);
                    }
                }).load();
    }
}
