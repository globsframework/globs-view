package org.globsframework.view.filter;

import org.globsframework.model.Glob;

public interface Filter {

    boolean isFiltered(Glob source);

}
