package org.globsframework.view.filter;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

import java.util.Map;

public interface FilterBuilder {
    FilterImpl.IsSelected create(Glob filter, GlobType rootType, Map<String, Glob> dico);
}
