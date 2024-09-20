package org.globsframework.view.filter;

import org.globsframework.core.model.Glob;

public interface Rewrite {
    Glob rewriteOrInAnd(Glob glob);
}
