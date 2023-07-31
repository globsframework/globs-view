package org.globsframework.view.filter;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.model.UniqueNameToPath;

public class FilterImpl implements Filter {
    private final IsSelected isSelected;

//    public FilterImpl(GlobType globType, Constraint constraint) {
//        isSelected = constraint.visit(new ViewConstraintVisitor(globType)).getIsSelected();
//    }

    public FilterImpl(GlobType globType, Glob globFilter, UniqueNameToPath uniqueNameToPath, boolean fullQuery) {
        isSelected = globFilter.getType().getRegistered(FilterBuilder.class)
                .create(globFilter, globType, uniqueNameToPath, fullQuery);
    }

    public boolean isFiltered(Glob source) {
        return isSelected.isSelected(source);
    }

    public interface IsSelected {
        boolean isSelected(Glob glob);
    }
}
