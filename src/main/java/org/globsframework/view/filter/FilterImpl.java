package org.globsframework.view.filter;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.view.filter.model.UniqueNameToPath;

public class FilterImpl implements Filter {
    private final IsSelected isSelected;

    public FilterImpl(IsSelected isSelected) {
        this.isSelected = isSelected;
    }

    public static Filter create(GlobType globType, Glob globFilter, UniqueNameToPath uniqueNameToPath, boolean fullQuery) {
        final IsSelected selected = globFilter.getType().getRegistered(FilterBuilder.class)
                .create(globFilter, globType, uniqueNameToPath, fullQuery);
        if (selected != null) {
            return new FilterImpl(selected);
        }
        return null;
    }

    public boolean isFiltered(Glob source) {
        return isSelected.isSelected(source);
    }

    public interface IsSelected {
        boolean isSelected(Glob glob);
    }
}
