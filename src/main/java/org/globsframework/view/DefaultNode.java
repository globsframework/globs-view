package org.globsframework.view;

import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.utils.container.hash.HashContainer;

import java.util.function.Function;

public class DefaultNode implements Node {
    private final Object key;
    private final String strValue;
    private String nodeName;
    private HashContainer<Object, Node> children = HashContainer.EMPTY_INSTANCE;
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

    public HashContainer<Object, Node> getChildren() {
        return children;
    }

    public MutableGlob getOutput() {
        return output;
    }

    public Node getOrCreate(Object value, Function<Object, Node> create) {
        Node node = children.get(value);
        if (node == null) {
            node = create.apply(value);
            children = children.put(value, node);
        }
        return node;
    }
}
