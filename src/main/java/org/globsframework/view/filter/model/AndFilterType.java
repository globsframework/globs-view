package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobArrayUnionField;
import org.globsframework.metamodel.fields.GlobUnionField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;

import java.util.Map;

public class AndFilterType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            GreaterOrEqualType.class, StrictlyGreaterType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class})
    public static GlobArrayUnionField filters;

    static {
        GlobTypeLoaderFactory.create(AndFilterType.class)
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, Map<String, Glob> dico){
                        Glob[] globs = filter.get(filters);
                        FilterImpl.IsSelected and[] = new FilterImpl.IsSelected[globs.length];
                        for (int i = 0, globsLength = globs.length; i < globsLength; i++) {
                            Glob glob = globs[i];
                            and[i] = glob.getType().getRegistered(FilterBuilder.class)
                                    .create(glob, rootType, dico);
                        }
                        return glob -> {
                            for (FilterImpl.IsSelected isSelected : and) {
                                if (!isSelected.isSelected(glob)) {
                                    return false;
                                }
                            }
                            return true;
                        };
                    }
                })
                .load();
    }
}
