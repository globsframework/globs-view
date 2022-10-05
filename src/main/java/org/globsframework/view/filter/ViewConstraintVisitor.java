package org.globsframework.view.filter;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DateTimeField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.model.globaccessor.get.GlobGetAccessor;
import org.globsframework.sqlstreams.constraints.ConstraintVisitor;
import org.globsframework.sqlstreams.constraints.OperandVisitor;
import org.globsframework.sqlstreams.constraints.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

public class ViewConstraintVisitor implements ConstraintVisitor {
    private final static Logger LOGGER = LoggerFactory.getLogger(ViewConstraintVisitor.class);
    private final GlobType globType;
    private FilterImpl.IsSelected isSelected;

    public ViewConstraintVisitor(GlobType globType) {
        this.globType = globType;
    }

    public FilterImpl.IsSelected getIsSelected() {
        return isSelected;
    }

    public void visitEqual(EqualConstraint equalConstraint) {
        GlobGetAccessor leftAccessor = equalConstraint.getLeftOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        GlobGetAccessor rightAccessor = equalConstraint.getRightOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        isSelected = glob -> Objects.equals(leftAccessor.getValue(glob), rightAccessor.getValue(glob));
    }

    public void visitNotEqual(NotEqualConstraint notEqualConstraint) {
        GlobGetAccessor leftAccessor = notEqualConstraint.getLeftOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        GlobGetAccessor rightAccessor = notEqualConstraint.getRightOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        isSelected = glob -> !Objects.equals(leftAccessor.getValue(glob), rightAccessor.getValue(glob));
    }

    public void visitAnd(AndConstraint andConstraint) {
        FilterImpl.IsSelected left = andConstraint.getLeftConstraint().visit(new ViewConstraintVisitor(globType)).isSelected;
        FilterImpl.IsSelected right = andConstraint.getRightConstraint().visit(new ViewConstraintVisitor(globType)).isSelected;
        isSelected = glob -> left.isSelected(glob) && right.isSelected(glob);
    }

    public void visitOr(OrConstraint orConstraint) {
        FilterImpl.IsSelected left = orConstraint.getLeftConstraint().visit(new ViewConstraintVisitor(globType)).isSelected;
        FilterImpl.IsSelected right = orConstraint.getRightConstraint().visit(new ViewConstraintVisitor(globType)).isSelected;
        isSelected = glob -> left.isSelected(glob) || right.isSelected(glob);
    }

    public void visitLessThan(LessThanConstraint lessThanConstraint) {
        DataAccessOperandVisitor operandVisitor = new DataAccessOperandVisitor();
        GlobGetAccessor leftAccessor = lessThanConstraint.getLeftOperand().visitOperand(operandVisitor).globGetAccessor;
        GlobGetAccessor rightAccessor = lessThanConstraint.getRightOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        Field field = operandVisitor.field;
        if (field instanceof DateTimeField) {
            isSelected = glob -> {
                ZonedDateTime leftValue = (ZonedDateTime) leftAccessor.getValue(glob);
                ZonedDateTime rightValue = ((ZonedDateTime) rightAccessor.getValue(glob));
                return leftValue == rightValue ||
                        (leftValue == null || (rightValue != null && (leftValue.isBefore(rightValue) || leftValue.isEqual(rightValue))));
            };
        } else if (field instanceof LongField) {
            isSelected = glob -> {
                Long leftValue = (Long) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || leftValue == null ||
                        (rightValue != null && (leftValue.longValue() <= rightValue.longValue()));
            };
        } else if (field instanceof IntegerField) {
            isSelected = glob -> {
                Integer leftValue = (Integer) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || leftValue == null ||
                        (rightValue != null && (leftValue.intValue() <= rightValue.intValue()));
            };
        } else if (field instanceof DoubleField) {
            isSelected = glob -> {
                Double leftValue = (Double) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || leftValue == null ||
                        (rightValue != null && (leftValue.doubleValue() <= rightValue.doubleValue()));
            };
        } else {
            String msg = "type " + field.getValueClass() + " not managed for " + field.getFullName();
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
    }

    public void visitBiggerThan(BiggerThanConstraint biggerThanConstraint) {
        DataAccessOperandVisitor operandVisitor = new DataAccessOperandVisitor();
        GlobGetAccessor leftAccessor = biggerThanConstraint.getLeftOperand().visitOperand(operandVisitor).globGetAccessor;
        GlobGetAccessor rightAccessor = biggerThanConstraint.getRightOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        Field field = operandVisitor.field;
        if (field instanceof DateTimeField) {
            isSelected = glob -> {
                ZonedDateTime leftValue = (ZonedDateTime) leftAccessor.getValue(glob);
                ZonedDateTime rightValue = ((ZonedDateTime) rightAccessor.getValue(glob));
                return leftValue == rightValue ||
                        (rightValue == null || (leftAccessor != null && (rightValue.isBefore(leftValue) || leftValue.isEqual(rightValue))));
            };
        } else if (field instanceof LongField) {
            isSelected = glob -> {
                Long leftValue = (Long) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || rightValue == null ||
                        (leftValue != null && (rightValue.longValue() <= leftValue.longValue()));
            };
        } else if (field instanceof IntegerField) {
            isSelected = glob -> {
                Integer leftValue = (Integer) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || rightValue == null ||
                        (leftValue != null && (rightValue.intValue() <= leftValue.intValue()));
            };
        } else if (field instanceof DoubleField) {
            isSelected = glob -> {
                Double leftValue = (Double) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || rightValue == null ||
                        (leftValue != null && (rightValue.doubleValue() <= leftValue.doubleValue()));
            };
        } else {
            String msg = "type " + field.getValueClass() + " not managed for " + field.getFullName();
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }

    }

    public void visitStrictlyBiggerThan(StrictlyBiggerThanConstraint strictlyBiggerThanConstraint) {
        DataAccessOperandVisitor operandVisitor = new DataAccessOperandVisitor();
        GlobGetAccessor leftAccessor = strictlyBiggerThanConstraint.getLeftOperand().visitOperand(operandVisitor).globGetAccessor;
        GlobGetAccessor rightAccessor = strictlyBiggerThanConstraint.getRightOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        Field field = operandVisitor.field;
        if (field instanceof DateTimeField) {
            isSelected = glob -> {
                ZonedDateTime leftValue = (ZonedDateTime) leftAccessor.getValue(glob);
                ZonedDateTime rightValue = ((ZonedDateTime) rightAccessor.getValue(glob));
                return leftValue == rightValue ||
                        (rightValue == null || (leftAccessor != null && (rightValue.isBefore(leftValue))));
            };
        } else if (field instanceof LongField) {
            isSelected = glob -> {
                Long leftValue = (Long) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || rightValue == null ||
                        (leftValue != null && (rightValue.longValue() < leftValue.longValue()));
            };
        } else if (field instanceof IntegerField) {
            isSelected = glob -> {
                Integer leftValue = (Integer) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || rightValue == null ||
                        (leftValue != null && (rightValue.intValue() < leftValue.intValue()));
            };
        } else if (field instanceof DoubleField) {
            isSelected = glob -> {
                Double leftValue = (Double) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || rightValue == null ||
                        (leftValue != null && (rightValue.doubleValue() < leftValue.doubleValue()));
            };
        } else {
            String msg = "type " + field.getValueClass() + " not managed for " + field.getFullName();
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }

    }

    public void visitStrictlyLesserThan(StrictlyLesserThanConstraint strictlyLesserThanConstraint) {
        DataAccessOperandVisitor operandVisitor = new DataAccessOperandVisitor();
        GlobGetAccessor leftAccessor = strictlyLesserThanConstraint.getLeftOperand().visitOperand(operandVisitor).globGetAccessor;
        GlobGetAccessor rightAccessor = strictlyLesserThanConstraint.getRightOperand().visitOperand(new DataAccessOperandVisitor()).globGetAccessor;
        Field field = operandVisitor.field;
        if (field instanceof DateTimeField) {
            isSelected = glob -> {
                ZonedDateTime leftValue = (ZonedDateTime) leftAccessor.getValue(glob);
                ZonedDateTime rightValue = ((ZonedDateTime) rightAccessor.getValue(glob));
                return leftValue == rightValue ||
                        (leftValue == null || (rightValue != null && (leftValue.isBefore(rightValue))));
            };
        } else if (field instanceof LongField) {
            isSelected = glob -> {
                Long leftValue = (Long) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || leftValue == null ||
                        (rightValue != null && (leftValue.longValue() < rightValue.longValue()));
            };
        } else if (field instanceof IntegerField) {
            isSelected = glob -> {
                Integer leftValue = (Integer) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || leftValue == null ||
                        (rightValue != null && (leftValue.intValue() < rightValue.intValue()));
            };
        } else if (field instanceof DoubleField) {
            isSelected = glob -> {
                Double leftValue = (Double) leftAccessor.getValue(glob);
                Number rightValue = (Number) rightAccessor.getValue(glob);
                return leftValue == rightValue || leftValue == null ||
                        (rightValue != null && (leftValue.doubleValue() < rightValue.doubleValue()));
            };
        } else {
            String msg = "type " + field.getValueClass() + " not managed for " + field.getFullName();
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
    }

    public void visitIn(InConstraint inConstraint) {
        Field field = inConstraint.getField();
        Set values = inConstraint.getValues();
        isSelected = glob -> values.contains(glob.getValue(field));

    }

    public void visitIsOrNotNull(NullOrNotConstraint nullOrNotConstraint) {
        Field field = nullOrNotConstraint.getField();
        isSelected = nullOrNotConstraint.checkNull() ? glob -> glob.isNull(field) : glob -> !glob.isNull(field);
    }

    public void visitNotIn(NotInConstraint notInConstraint) {
        Field field = notInConstraint.getField();
        Set values = notInConstraint.getValues();
        isSelected = glob -> !values.contains(glob.getValue(field));
    }

    public void visitContains(Field field, String s, boolean b, boolean startWith) {
        if (startWith) {
            if (b) {
                isSelected = glob -> glob.get(field.asStringField(), "").startsWith(s);
            } else {
                isSelected = glob -> !glob.get(field.asStringField(), "").startsWith(s);
            }
        }
        else {
            if (b) {
                isSelected = glob -> glob.get(field.asStringField(), "").contains(s);
            } else {
                isSelected = glob -> !glob.get(field.asStringField(), "").contains(s);
            }
        }
    }

    private static class DataAccessOperandVisitor implements OperandVisitor {
        GlobGetAccessor globGetAccessor;
        Field field;

        public void visitValueOperand(ValueOperand valueOperand) {
            field = valueOperand.getField();
            globGetAccessor = glob -> valueOperand.getValue();
        }

        public void visitAccessorOperand(AccessorOperand accessorOperand) {
            field = accessorOperand.getField();
            globGetAccessor = glob -> accessorOperand.getAccessor();
        }

        public void visitFieldOperand(Field field) {
            this.field = field;
            globGetAccessor = glob -> glob.getValue(field);
        }
    }
}
