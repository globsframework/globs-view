package org.globsframework.view.filter.model;

import java.util.Arrays;

public interface UniqueNameToPath {
    record PathField(String[] path, String name) {
        @Override
        public String toString() {
            return "PathField{" +
                    "path=" + Arrays.toString(path) +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    PathField get(String uniqueName);
}
