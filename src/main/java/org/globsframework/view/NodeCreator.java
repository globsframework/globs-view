package org.globsframework.view;

import org.globsframework.core.model.Glob;

public interface NodeCreator {

    Node getOrCreate(Node parent, Glob data) throws TooManyNodeException;
}
