package org.globsframework.view.filter;

import org.globsframework.model.Glob;

import java.util.function.Consumer;

public interface WantedField {
    void wanted(Glob filter, Consumer<String> wantedUniqueName);
}
