package org.globsframework.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SimpleGraph<T> {
    Map<String, SimpleGraph<T>> children = new HashMap<>();
    String name;
    T value;

    public SimpleGraph(String name, T value) {
        this.name = name;
        this.value = value;
    }

    SimpleGraph<T> getOrCreate(String name, T value) {
        return children.computeIfAbsent(name, s -> new SimpleGraph<T>(name, value));
    }

    static class Visitor<T> {
        Stack<SimpleGraph<T>> nodes = new Stack<>();
        SimpleGraph<T> NULL;

        public Visitor(SimpleGraph<T> node, T defaultValue) {
            nodes.push(node);
            NULL = new SimpleGraph<T>("", defaultValue);
        }

        void enter(String name) {
            nodes.push(nodes.peek().children.getOrDefault(name, NULL));
        }

        T getValue() {
            return nodes.peek().value;
        }

        void leave() {
            nodes.pop();
        }
    }
}
