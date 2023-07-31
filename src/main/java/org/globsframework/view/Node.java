package org.globsframework.view;

import org.globsframework.model.MutableGlob;
import org.globsframework.utils.container.Container;

import java.util.function.Function;

public interface Node {

    String getName();

    Object getKey();

    String getKeyAsString();

    Container<Comparable, Node> getChildren();

    MutableGlob getOutput();

    Node getOrCreate(Comparable value, Function<Object, Node> create);
}
