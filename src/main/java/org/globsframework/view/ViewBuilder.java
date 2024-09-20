package org.globsframework.view;

import org.globsframework.core.metamodel.GlobType;

public interface ViewBuilder {

    GlobType getBreakdownType();

    GlobType getOutputType();

    View createView();

}
