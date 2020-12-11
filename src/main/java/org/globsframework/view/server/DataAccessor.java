package org.globsframework.view.server;

import org.globsframework.model.Glob;

import java.util.List;

public interface DataAccessor {
    List<Glob> getSources();

    Source getSource(String source);
}
