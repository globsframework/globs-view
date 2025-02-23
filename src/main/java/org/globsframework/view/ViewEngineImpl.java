package org.globsframework.view;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.metamodel.type.DataType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.view.model.DictionaryType;
import org.globsframework.view.model.IsSensibleData;
import org.globsframework.view.model.SimpleBreakdown;
import org.globsframework.view.model.StringAsDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class ViewEngineImpl implements ViewEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewEngineImpl.class);
    public static final int VIEW_MAX_DEPTH = Integer.getInteger("globs.view.max.depth", 10);

    public ViewBuilder buildView(Glob dictionary, Glob viewRequestType, int maxNodeCount) {
        return new ViewBuilderImpl(dictionary, viewRequestType, maxNodeCount);
    }

    public Glob createDictionary(GlobType globType) {
        List<Glob> breakdowns = new ArrayList<>();
        extract(globType, breakdowns, new ArrayDeque<>());
        return DictionaryType.TYPE.instantiate().set(DictionaryType.breakdowns, breakdowns.toArray(new Glob[0]));
    }

    private void extract(GlobType globType, List<Glob> breakdowns, ArrayDeque<String> path) {
        LOGGER.debug("extract " + globType.getName());
        if (path.size() > VIEW_MAX_DEPTH) {
            LOGGER.warn("Stop drilling down to deep in fields : " + String.join("/", path));
            return;
        }
        Field[] fields = globType.getFields();
        for (Field field : fields) {
            if (field.getDataType().isPrimive() /*&& !field.getDataType().isArray() */) {
                String[] strings = path.toArray(String[]::new);
                String join = String.join(".", strings);
                String uniqueName = join + (join.isEmpty() ? "" : ".") + field.getName();
                MutableGlob brk = SimpleBreakdown.TYPE.instantiate()
                        .set(SimpleBreakdown.path, strings)
                        .set(SimpleBreakdown.uniqueName, uniqueName)
                        .set(SimpleBreakdown.aliasName, uniqueName)
                        .set(SimpleBreakdown.typeName, globType.getName())
                        .set(SimpleBreakdown.fieldName, field.getName())
                        .set(SimpleBreakdown.isSensibleData, field.hasAnnotation(IsSensibleData.UNIQUE_KEY))
                        .set(SimpleBreakdown.nativeType, field.getDataType().name());
                if (field.hasAnnotation(StringAsDouble.UNIQUE_KEY)) {
                    brk.set(SimpleBreakdown.outputTypeName, DataType.Double.name());
                }
                breakdowns.add(brk);
            } else if (field instanceof GlobArrayField) {
                path.addLast(field.getName());
                extract(((GlobArrayField) field).getTargetType(), breakdowns, path);
                path.removeLast();
            } else if (field instanceof GlobField) {
                path.addLast(field.getName());
                extract(((GlobField) field).getTargetType(), breakdowns, path);
                path.removeLast();
            } else if (field instanceof GlobUnionField) {
                path.addLast(field.getName());
                for (GlobType targetType : ((GlobUnionField) field).getTargetTypes()) {
                    path.addLast(targetType.getName());
                    extract(targetType, breakdowns, path);
                    path.removeLast();
                }
                path.removeLast();
            } else if (field instanceof GlobArrayUnionField) {
                path.addLast(field.getName());
                for (GlobType targetType : ((GlobArrayUnionField) field).getTargetTypes()) {
                    path.addLast(targetType.getName());
                    extract(targetType, breakdowns, path);
                    path.removeLast();
                }
                path.removeLast();
            }
        }
    }

}
