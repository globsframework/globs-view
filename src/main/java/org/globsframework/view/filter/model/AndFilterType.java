package org.globsframework.view.filter.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Targets;
import org.globsframework.core.metamodel.fields.GlobArrayUnionField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.utils.exceptions.ItemNotFound;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AndFilterType {
    public static GlobType TYPE;

    @Targets({OrFilterType.class, AndFilterType.class, EqualType.class, NotEqualType.class,
            NotType.class, GreaterOrEqualType.class, StrictlyGreaterType.class,
            StrictlyLessType.class, LessOrEqualType.class, ContainsType.class, NotContainsType.class, IsNullType.class, IsNotNullType.class})
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
                .register(Rewrite.class, new Rewrite() {
                    public Glob rewriteOrInAnd(Glob glob) {
                        List<Glob> predicate = new ArrayList<>();
                        for (Glob value : glob.getOrEmpty(filters)) {
                            Glob p = value != null ? value.getType().getRegistered(Rewrite.class)
                                    .rewriteOrInAnd(value) : null;
                            if (p != null) {
                                predicate.add(p);
                            }
                        }
                        if (predicate.isEmpty()) {
                            return null;
                        }
                        if (predicate.size() == 1) {
                            return predicate.get(0);
                        }
                        return TYPE.instantiate().set(filters, predicate.toArray(Glob[]::new));
                    }
                })
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico, boolean fullQuery) {
                        List<FilterImpl.IsSelected> and = new ArrayList<>();
                        for (Glob glob : filter.getOrEmpty(filters)) {
                            try {
                                final FilterImpl.IsSelected isSelected = glob.getType().getRegistered(FilterBuilder.class)
                                        .create(glob, rootType, dico, fullQuery);
                                if (isSelected != null) {
                                    and.add(isSelected);
                                }
                            } catch (ItemNotFound e) {
                                if (fullQuery) {
                                    throw e;
                                }
                            }
                        }
                        if (and.isEmpty()) {
                            return null;
                        }
                        if (and.size() == 1) {
                            return and.get(0);
                        } else {
                            FilterImpl.IsSelected[] andArray = and.toArray(FilterImpl.IsSelected[]::new);
                            return glob -> {
                                for (FilterImpl.IsSelected isSelected : andArray) {
                                    if (!isSelected.isSelected(glob)) {
                                        return false;
                                    }
                                }
                                return true;
                            };
                        }
                    }
                })
                .load();
    }
}
