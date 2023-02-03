package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobUnionField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.WantedField;

public class NotType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            GreaterOrEqualType.class, StrictlyGreaterType.class,
            NotType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class, NotContainsType.class})
    public static GlobUnionField filter;

    static {
        GlobTypeLoaderFactory.create(NotType.class)
                .register(WantedField.class, (filter, wantedUniqueName) -> filter.getOptional(NotType.filter)
                        .ifPresent(glob -> glob.getType().getRegistered(WantedField.class)
                                .wanted(glob, wantedUniqueName)))
                .register(FilterBuilder.class, (filter, rootType, dico) -> {
                    Glob glFilter = filter.get(NotType.filter);
                    final FilterImpl.IsSelected selected = glFilter.getType().getRegistered(FilterBuilder.class)
                            .create(glFilter, rootType, dico);
                    return glob -> !selected.isSelected(glob);
                }).load();
    }
}
