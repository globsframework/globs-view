package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobUnionField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.WantedField;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class FilterType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            GreaterOrEqualType.class, StrictlyGreaterType.class, NotType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class, NotContainsType.class, IsNullType.class, IsNotNullType.class})
    public static GlobUnionField filter;

    static {
        GlobTypeLoaderFactory.create(FilterType.class)
                .register(WantedField.class, new WantedField() {
                    public void wanted(Glob f, Consumer<String> wantedUniqueName) {
                        Stream.ofNullable(f.get(filter))
                                .forEach(glob -> glob.getType().getRegistered(WantedField.class)
                                        .wanted(glob, wantedUniqueName));
                    }
                })
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob globFilter, GlobType rootType, UniqueNameToPath dico) {
                        Glob glob = globFilter.get(filter);
                        if (glob == null) {
                            return glob1 -> true;
                        }
                        return glob.getType().getRegistered(FilterBuilder.class)
                                .create(glob, rootType, dico);
                    }
                }).load();
    }
}
