package org.globsframework.view;

import org.globsframework.model.MutableGlob;
import org.globsframework.utils.container.Container;

import java.util.function.Function;

public class DefaultNode implements Node {
    private final Object key;
    private final String strValue;
    private String nodeName;
    private Container<Comparable, Node> children = Container.EMPTY_INSTANCE;
    private MutableGlob output;

    public DefaultNode(String nodeName, Object key, String strValue, MutableGlob output) {
        this.nodeName = nodeName;
        this.key = key;
        this.strValue = strValue;
        this.output = output;
    }

    public String getName() {
        return nodeName;
    }

    public Object getKey() {
        return key;
    }

    public String getKeyAsString() {
        return strValue;
    }

    public Container<Comparable, Node> getChildren() {
        return children;
    }

    public MutableGlob getOutput() {
        return output;
    }

    public Node getOrCreate(Comparable value, Function<Object, Node> create) {
        Node node = children.get(value);
        if (node == null) {
            node = create.apply(value);
            children = children.put(value, node);
        }
        return node;
    }
}
