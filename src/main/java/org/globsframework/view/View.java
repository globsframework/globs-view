package org.globsframework.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.Filter;
import org.globsframework.view.filter.FilterImpl;
import org.globsframework.view.server.Source;

import java.util.Optional;

public interface View {

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
