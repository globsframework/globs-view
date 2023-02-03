package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobArrayUnionField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.WantedField;

import java.util.Arrays;
import java.util.function.Consumer;

public class AndFilterType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            NotType.class, GreaterOrEqualType.class, StrictlyGreaterType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class, NotContainsType.class})
    public static GlobArrayUnionField filters;

    static {
        GlobTypeLoaderFactory.create(AndFilterType.class)
                .register(WantedField.class, new WantedField() {
                    public void wanted(Glob filter, Consumer<String> wantedUniqueName) {
                        Arrays.stream(filter.getOrEmpty(filters))
                                .forEach(glob -> glob.getType().getRegistered(WantedField.class)
                                        .wanted(glob, wantedUniqueName));
                    }
                })
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico){
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
