package org.globsframework.view.filter.model;

public interface UniqueNameToPath {
    record Field(String[] path, String name) {
    }

    Field get(String uniqueName);
}
