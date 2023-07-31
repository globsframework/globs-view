package org.globsframework.view;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.FieldVisitor;
import org.globsframework.metamodel.fields.IntegerArrayField;
import org.globsframework.metamodel.fields.StringArrayField;
import org.globsframework.model.Glob;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public class DefaultNodeCreator implements NodeCreator, Function<Object, Node> {
    private final GlobType outputType;
    private final String nodeName;
    private final Field field;
    private PathBaseViewImpl.NotifyNode notifyNode;
    private final ToString toString;
    private final ToValue toValue;

    public DefaultNodeCreator(GlobType outputType, String nodeName, Field field, PathBaseViewImpl.NotifyNode notifyNode) {
        this.outputType = outputType;
        this.nodeName = nodeName;
        this.field = field;
        this.notifyNode = notifyNode;
        ToStringVisitor visitor = new ToStringVisitor();
        field.safeVisit(visitor);
        toString = visitor.toString;
        toValue = visitor.toValue;
    }

    public Node getOrCreate(Node parent, Glob value) {
        return parent.getOrCreate(((Comparable) value.getValue(field)), this);
    }

    public Node apply(Object o) {
        notifyNode.newNode();
        return new DefaultNode(nodeName, toValue.toValue(o), toString.toString(o), outputType.getFields().length != 0 ? outputType.instantiate() : null);
    }

    interface ToString {
        String toString(Object o);
    }

    interface ToValue{
        Object toValue(Object v);
    }

    static class ToStringVisitor extends FieldVisitor.AbstractFieldVisitor {
        ToString toString;
        ToValue toValue;

        public void visitIntegerArray(IntegerArrayField field) throws Exception {
            toString = a -> Arrays.toString(((int[]) a));
            toValue = v -> v;
        }

        public void visitStringArray(StringArrayField field) throws Exception {
            toString = a -> Arrays.toString(((String[]) a));
            toValue = v -> v;
        }

        public void notManaged(Field field) throws Exception {
            toString = Objects::toString;
            toValue = v -> v;
        }
    }
}
