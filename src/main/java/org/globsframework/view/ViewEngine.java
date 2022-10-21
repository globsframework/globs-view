package org.globsframework.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

public interface ViewEngine {
    int DEFAULT_MAX_NODE = Integer.getInteger("globs.view.max.node", 500000);

    default ViewBuilder buildView(Glob dictionary, Glob viewRequestType) {
        return buildView(dictionary, viewRequestType, DEFAULT_MAX_NODE);
    }

    ViewBuilder buildView(Glob dictionary, Glob viewRequestType, int maxNodeCount);

    Glob createDictionary(GlobType globType);


}
