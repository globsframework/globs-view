package org.globsframework.view;

import org.globsframework.model.MutableGlob;

import java.util.Map;
import java.util.function.Function;

public interface Node {

    String getName();

    Object getKey();

    String getKeyAsString();

    Map<Object, Node> getChildren();

    MutableGlob getOutput();

    Node getOrCreate(Object value, Function<Object, Node> create);
}
