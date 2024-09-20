package org.globsframework.view;

import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.view.filter.Rewrite;
import org.globsframework.view.filter.model.FilterType;
import org.globsframework.view.filter.model.OrFilterType;
import org.junit.Assert;
import org.junit.Test;

public class FilterTest {
    @Test
    public void simpleFilter() {
        final MutableGlob set = FilterType.TYPE.instantiate()
                .set(FilterType.filter, OrFilterType.TYPE.instantiate()
                        .set(OrFilterType.filters, new Glob[0]));
        final Glob glob = FilterType.TYPE.getRegistered(Rewrite.class)
                .rewriteOrInAnd(set);
        Assert.assertNull(glob);
    }
}
