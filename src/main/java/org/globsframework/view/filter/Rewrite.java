package org.globsframework.view.filter;

import org.globsframework.model.Glob;

public interface Rewrite {
    Glob rewriteOrInAnd(Glob glob);
}
