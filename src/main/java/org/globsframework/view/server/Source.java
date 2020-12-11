package org.globsframework.view.server;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.view.View;
import org.globsframework.view.ViewBuilder;

import java.util.function.Consumer;

public interface Source {

    String getID();

    GlobType getOutputType();

    DataConsumer create(View.Accepted accepted);
//     {
//        return new DataConsumer() {
//            public GlobType getOutputType() {
//                return Source.this.getOutputType();
//            }
//
//            public void getAll(Consumer<Glob> consumer) {
//                Source.this.getAll(consumer);
//            }
//        };
//    }

//    void getAll(Consumer<Glob> consumer);

    interface DataConsumer {

        GlobType getOutputType();

        void getAll(Consumer<Glob> consumer);
    }

}
