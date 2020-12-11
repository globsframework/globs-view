package org.globsframework.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

public interface ViewBuilder {

    GlobType getBreakdownType();

    GlobType getOutputType();

    View createView();

}
