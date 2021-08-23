package org.globsframework.view;

import org.globsframework.model.MutableGlob;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DefaultNode implements Node {
    private final Object key;
    private final String strValue;
    private String nodeName;
    private Map<Object, Node> children;
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

    public Map<Object, Node> getChildren() {
        return children == null ? Collections.emptyMap() : children;
    }

    public MutableGlob getOutput() {
        return output;
    }

    public Node getOrCreate(Object value, Function<Object, Node> create) {
        if (children == null) {
            children = new HashMap<>();
        }
        return children.computeIfAbsent(value, create);
    }
}
