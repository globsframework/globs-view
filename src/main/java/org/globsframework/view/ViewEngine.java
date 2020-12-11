package org.globsframework.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

public interface ViewEngine {

    ViewBuilder buildView(Glob dictionary, Glob viewRequestType);

    Glob createDictionary(GlobType globType);


}
