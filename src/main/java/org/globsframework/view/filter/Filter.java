package org.globsframework.view.filter;

import org.globsframework.core.model.Glob;

public interface Filter {

    boolean isFiltered(Glob data);

}
