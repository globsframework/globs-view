package org.globsframework.view;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

import java.util.Objects;
import java.util.function.Function;

public class DefaultNodeCreator implements NodeCreator, Function<Object, Node> {
    private final GlobType outputType;
    private final String nodeName;
    private final Field field;

    public DefaultNodeCreator(GlobType outputType, String nodeName, Field field) {
        this.outputType = outputType;
        this.nodeName = nodeName;
        this.field = field;
    }

    public Node getOrCreate(Node parent, Glob value) {
        return parent.getOrCreate(value.getValue(field), this);
    }

    public Node apply(Object o) {
        StringBuilder str = new StringBuilder();
        field.toString(str, o);
        return new DefaultNode(nodeName, o, Objects.toString(o), outputType.getFields().length != 0 ? outputType.instantiate() : null);
    }
}
