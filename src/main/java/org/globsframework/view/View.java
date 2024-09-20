package org.globsframework.view;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.view.filter.Filter;
import org.globsframework.view.server.Source;

public interface View {

    void reset();

    Append getAppender(GlobType globType);

    Node getRootNode();

    Glob toGlob();

    Filter getIndexFilter(GlobType index, Source.IndexFieldRemap indexFieldRemap);

    interface Append {
        void add(Glob glob);

        void complete();
    }

    Accepted getAccepter();

    interface Accepted {
        void enter(String name);

        boolean wanted();

        void leave();
    }

}
