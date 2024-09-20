package org.globsframework.view.filter.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Targets;
import org.globsframework.core.metamodel.fields.GlobArrayUnionField;
import org.globsframework.core.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class OrFilterType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            NotType.class,
            GreaterOrEqualType.class, StrictlyGreaterType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class, NotContainsType.class, IsNullType.class, IsNotNullType.class})
    public static GlobArrayUnionField filters;

    static {
        GlobTypeLoaderFactory.create(OrFilterType.class)
                .register(WantedField.class, new WantedField() {
                    public void wanted(Glob filter, Consumer<String> wantedUniqueName) {
                        Arrays.stream(filter.getOrEmpty(filters))
                                .forEach(glob -> glob.getType().getRegistered(WantedField.class)
                                        .wanted(glob, wantedUniqueName));
                    }
                })
                .register(Rewrite.class, new Rewrite() {
                    public Glob rewriteOrInAnd(Glob glob) {
                        final Glob[] gl = glob.getOrEmpty(filters);
                        List<Glob> predicate = new ArrayList<>();

                        for (int i = 0; i < gl.length; i++) {
                            final Glob value = gl[i] != null ? gl[i].getType().getRegistered(Rewrite.class)
                                    .rewriteOrInAnd(gl[i]) : null;
                            if (value == null) {
                            } else if (value.getType() == NotType.TYPE) { // remove not if not not
                                predicate.add(value.get(NotType.filter));
                            } else {
                                predicate.add(NotType.TYPE.instantiate().set(NotType.filter, value));
                            }
                        }
                        if (predicate.isEmpty()) {
                            return null;
                        }
                        if (predicate.size() == 1) {
                            return NotType.TYPE.instantiate().set(NotType.filter, predicate.get(0));
                        }
                        return NotType.TYPE.instantiate()
                                .set(NotType.filter, AndFilterType.TYPE.instantiate().set(AndFilterType.filters,
                                        predicate.toArray(Glob[]::new)));
                    }
                })

                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico, boolean fullQuery) {
                        List<FilterImpl.IsSelected> or = new ArrayList<>();
                        for (Glob glob : filter.getOrEmpty(filters)) {
                            FilterImpl.IsSelected e = glob != null ? glob.getType().getRegistered(FilterBuilder.class)
                                    .create(glob, rootType, dico, fullQuery) : null;
                            if (e != null) {
                                or.add(e);
                            }
                        }
                        if (or.isEmpty()) {
                            return null;
                        }
                        if (or.size() == 1) {
                            return or.get(0);
                        }
                        FilterImpl.IsSelected[] orArray = or.toArray(FilterImpl.IsSelected[]::new);
                        return glob -> {
                            for (FilterImpl.IsSelected isSelected : orArray) {
                                if (isSelected.isSelected(glob)) {
                                    return true;
                                }
                            }
                            return false;
                        };
                    }
                }).load();
    }
}
