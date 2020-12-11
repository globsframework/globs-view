package org.globsframework.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

public interface View {

    Append getAppender(GlobType globType);

    Node getRootNode();

    Glob toGlob();

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
