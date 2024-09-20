package org.globsframework.view.server;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.view.View;
import org.globsframework.view.filter.Filter;
import org.globsframework.view.filter.model.UniqueNameToPath;

import java.util.function.Consumer;

public interface Source {

    String getID();

    GlobType getOutputType();

    DataConsumer create(View.Accepted accepted);

    interface DataConsumer {

        default IndexFieldRemap getIndexRemap() {
            return null;
        }

        default GlobType getIndex() {
            return null;
        }

        GlobType getOutputType();

        void getAll(Consumer<Glob> consumer);

        default void getAll(Consumer<Glob> consumer, Filter indexFilter) {
            getAll(consumer);
        }
    }

    interface IndexFieldRemap {
        UniqueNameToPath.PathField translate(String uniqueName);
    }

}
