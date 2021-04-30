package org.globsframework.view;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.model.Glob;
import org.globsframework.view.model.DictionaryType;
import org.globsframework.view.model.SimpleBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ViewEngineImpl implements ViewEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewEngineImpl.class);
    public ViewBuilder buildView(Glob dictionary, Glob viewRequestType) {
        return new ViewBuilderImpl(dictionary, viewRequestType);
    }

    public Glob createDictionary(GlobType globType) {
        List<Glob> breakdowns = new ArrayList<>();
        extract(globType, breakdowns, new ArrayDeque<>());
        return DictionaryType.TYPE.instantiate().set(DictionaryType.breakdowns, breakdowns.toArray(new Glob[0]));
    }

    private void extract(GlobType globType, List<Glob> breakdowns, ArrayDeque<Field> path) {
        LOGGER.debug("extract " + globType.getName());
        Field[] fields = globType.getFields();
        for (Field field : fields) {
            if (field.getDataType().isPrimive() && !field.getDataType().isArray()) {
                String[] strings = path.stream().map(Field::getName).toArray(String[]::new);
                String join = String.join(".", strings);
                String uniqueName = join + (join.isEmpty() ? "" : ".") + field.getName();
                breakdowns.add(SimpleBreakdown.TYPE.instantiate()
                        .set(SimpleBreakdown.path, strings)
                        .set(SimpleBreakdown.uniqueName, uniqueName)
                        .set(SimpleBreakdown.aliasName, uniqueName)
                        .set(SimpleBreakdown.typeName, globType.getName())
                        .set(SimpleBreakdown.fieldName, field.getName())
                        .set(SimpleBreakdown.nativeType, field.getDataType().name())
                );
            } else if (field instanceof GlobArrayField) {
                path.addLast(field);
                extract(((GlobArrayField) field).getTargetType(), breakdowns, path);
                path.removeLast();
            } else if (field instanceof GlobField) {
                path.addLast(field);
                extract(((GlobField) field).getTargetType(), breakdowns, path);
                path.removeLast();
            }
        }
    }

}
