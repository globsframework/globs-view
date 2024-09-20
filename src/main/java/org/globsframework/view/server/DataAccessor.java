package org.globsframework.view.server;

import org.globsframework.core.model.Glob;

import java.util.List;

public interface DataAccessor {
    List<Glob> getSources();

    Source getSource(String source);
}
