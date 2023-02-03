package org.globsframework.view.filter;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.model.UniqueNameToPath;

public interface FilterBuilder {
    FilterImpl.IsSelected create(Glob filter, GlobType rootType, UniqueNameToPath dico);
}
