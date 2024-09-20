package org.globsframework.view.filter.model;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Targets;
import org.globsframework.core.metamodel.fields.GlobUnionField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.utils.exceptions.ItemNotFound;
import org.globsframework.view.filter.FilterBuilder;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.filter.Rewrite;
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
                    public FilterImpl.IsSelected create(Glob globFilter, GlobType rootType, UniqueNameToPath dico, boolean fullQuery) {
                        Glob glob = globFilter.get(filter);
                        if (glob == null) {
                            return null;
                        }
                        try {
                            return glob.getType().getRegistered(FilterBuilder.class)
                                    .create(glob, rootType, dico, fullQuery);
                        } catch (ItemNotFound e) {
                            if (fullQuery) {
                                throw e;
                            }
                        }
                        return null;
                    }
                })
                .register(Rewrite.class, new Rewrite() {
                    public Glob rewriteOrInAnd(Glob glob) {
                        final Glob gl = glob.get(filter);
                        if (gl != null) {
                            final Glob value = gl.getType().getRegistered(Rewrite.class).rewriteOrInAnd(gl);
                            if (value == null) {
                                return null;
                            }
                            return TYPE.instantiate().set(filter, value);
                        }
                        return gl;
                    }
                })
                .load();
    }
}
