package org.globsframework.view.filter.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobArrayUnionField;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.WantedField;

import java.util.Arrays;
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
                        if (gl.length == 0) {
                            return null;
                        }
                        for (int i = 0; i < gl.length; i++) {
                            final Glob value = gl[i] != null ? gl[i].getType().getRegistered(Rewrite.class)
                                    .rewriteOrInAnd(gl[i]) : null;
                            if (value == null) {
                                gl[i] = null;
                            }else if (value.getType() == NotType.TYPE) { // remove not if not not
                                gl[i] = value.get(NotType.filter);
                            }else {
                                gl[i] = NotType.TYPE.instantiate().set(NotType.filter, value);
                            }
                        }
                        return NotType.TYPE.instantiate()
                                .set(NotType.filter, AndFilterType.TYPE.instantiate().set(AndFilterType.filters, gl));
                    }
                })

                .register(FilterBuilder.class, new FilterBuilder() {
                    public FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico, boolean fullQuery){
                        Glob[] globs = filter.get(filters);
                        FilterImpl.IsSelected[] or = new FilterImpl.IsSelected[globs.length];
                        for (int i = 0, globsLength = globs.length; i < globsLength; i++) {
                            Glob glob = globs[i];
                            or[i] = glob != null ? glob.getType().getRegistered(FilterBuilder.class)
                                    .create(glob, rootType, dico, fullQuery) : g -> false;
                        }
                        return glob -> {
                            for (FilterImpl.IsSelected isSelected : or) {
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
