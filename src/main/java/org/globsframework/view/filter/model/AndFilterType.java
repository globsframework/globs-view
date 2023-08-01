package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobArrayUnionField;
import org.globsframework.model.Glob;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;

import java.util.Arrays;
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
                        final Glob[] gl = glob.getOrEmpty(filters);
                        if (gl.length == 0) {
                            return null;
                        }
                        for (int i = 0; i < gl.length; i++) {
                            gl[i] = gl[i] != null ? gl[i].getType().getRegistered(Rewrite.class)
                                    .rewriteOrInAnd(gl[i]) : null;
                        }
                        return TYPE.instantiate().set(filters, gl);
                    }
                })
                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico, boolean fullQuery) {
                        Glob[] globs = filter.getOrEmpty(filters);
                        FilterImpl.IsSelected[] and = new FilterImpl.IsSelected[globs.length];
                        for (int i = 0, globsLength = globs.length; i < globsLength; i++) {
                            Glob glob = globs[i];
                            try {
                                if (glob == null) {
                                    and[i] = g -> true;
                                }
                                else {
                                    and[i] = glob.getType().getRegistered(FilterBuilder.class)
                                            .create(glob, rootType, dico, fullQuery);
                                }
                            } catch (ItemNotFound e) {
                                if (!fullQuery) {
                                    and[i] = g -> true;
                                } else {
                                    throw e;
                                }
                            }
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
