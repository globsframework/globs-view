package org.globsframework.view;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.annotations.Targets;
import org.globsframework.metamodel.fields.GlobUnionField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringArrayField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.MutableGlob;
import org.globsframework.view.model.DictionaryType;
import org.globsframework.view.model.ViewBreakdown;
import org.globsframework.view.model.ViewOutput;
import org.globsframework.view.model.ViewRequestType;

import static org.globsframework.view.ViewEngineImplTest.br;

public class ViewOnGLobUnionTest extends TestCase {


    public void testWihtStringArray() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ObjectWithUnion.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("key", breakdowns),
                br("keyObj1", breakdowns),
                br("keyObj2", breakdowns)
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

        MutableGlob d1 = ObjectWithUnion.TYPE.instantiate()
                .set(ObjectWithUnion.key, "n1")
                .set(ObjectWithUnion.data, Object1.TYPE.instantiate().set(Object1.keyObj1, "data1"));
        MutableGlob d2 = ObjectWithUnion.TYPE.instantiate()
                .set(ObjectWithUnion.key, "n2")
                .set(ObjectWithUnion.data, Object2.TYPE.instantiate().set(Object2.keyObj2, "data2"));
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ObjectWithUnion.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\"name\":\"\",\"nodeName\":\"root\",\"__children__\":[{\"name\":\"n1\",\"nodeName\":\"key\",\"__children__\":[{\"name\":\"data1\",\"nodeName\":\"data.object1.keyObj1\"}]},{\"name\":\"n2\",\"nodeName\":\"key\"}]}"), GSonUtils.normalize(actual));
    }

    public void testWithOutput() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ObjectWithUnion.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("key", breakdowns),
                br("keyObj1", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate()
                        .set(ViewOutput.uniqueName, br("count_1", breakdowns).get(ViewBreakdown.uniqueName))
                        .set(ViewOutput.name, "quantity")
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

        MutableGlob d1 = ObjectWithUnion.TYPE.instantiate()
                .set(ObjectWithUnion.key, "n1")
                .set(ObjectWithUnion.data, Object1.TYPE.instantiate().set(Object1.keyObj1, "data1").set(Object1.count_1, 10));
        MutableGlob d2 = ObjectWithUnion.TYPE.instantiate()
                .set(ObjectWithUnion.key, "n2")
                .set(ObjectWithUnion.data, Object1.TYPE.instantiate().set(Object1.keyObj1, "data2").set(Object1.count_1, 2));
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ObjectWithUnion.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\"name\":\"\",\"nodeName\":\"root\",\"__children__\":[{\"name\":\"n1\",\"nodeName\":\"key\",\"__children__\":[{\"name\":\"data1\",\"nodeName\":\"data.object1.keyObj1\",\"output\":{\"quantity\":10}}],\"output\":{\"quantity\":10}},{\"name\":\"n2\",\"nodeName\":\"key\",\"__children__\":[{\"name\":\"data2\",\"nodeName\":\"data.object1.keyObj1\",\"output\":{\"quantity\":2}}],\"output\":{\"quantity\":2}}],\"output\":{\"quantity\":12}}"), GSonUtils.normalize(actual));
    }

    public static class Object1 {
        public static GlobType TYPE;

        public static StringField keyObj1;

        public static IntegerField count_1;

        static {
            GlobTypeLoaderFactory.create(Object1.class).load();
        }
    }

    public static class Object2 {
        public static GlobType TYPE;

        public static StringField keyObj2;

        public static IntegerField count_2;

        static {
            GlobTypeLoaderFactory.create(Object2.class).load();
        }
    }

    public static class ObjectWithUnion {
        public static GlobType TYPE;

        public static StringField key;

        @Targets({Object1.class, Object2.class})
        public static GlobUnionField data;

        static {
            GlobTypeLoaderFactory.create(ObjectWithUnion.class).load();
        }
    }
}
