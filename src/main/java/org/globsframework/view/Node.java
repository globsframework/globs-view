package org.globsframework.view;

import org.globsframework.model.MutableGlob;
import org.globsframework.utils.container.Container;
import org.globsframework.utils.container.hash.HashContainer;

import java.util.function.Function;

public interface Node {

    String getName();

    Object getKey();

    String getKeyAsString();

    HashContainer<Object, Node> getChildren();

    MutableGlob getOutput();

    Node getOrCreate(Object value, Function<Object, Node> create);
}
