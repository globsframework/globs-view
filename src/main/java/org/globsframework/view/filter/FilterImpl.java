package org.globsframework.view.filter;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;

import java.util.Map;

public class FilterImpl implements Filter {
    private final IsSelected isSelected;

//    public FilterImpl(GlobType globType, Constraint constraint) {
//        isSelected = constraint.visit(new ViewConstraintVisitor(globType)).getIsSelected();
//    }

    public FilterImpl(GlobType globType, Glob globFilter, Map<String, Glob> aliasToDico) {
        isSelected = globFilter.getType().getRegistered(FilterBuilder.class)
                .create(globFilter, globType, aliasToDico);
    }

    public boolean isFiltered(Glob source) {
        return isSelected.isSelected(source);
    }

    public interface IsSelected {
        boolean isSelected(Glob glob);
    }
}
