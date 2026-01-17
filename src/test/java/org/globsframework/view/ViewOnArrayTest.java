package org.globsframework.view;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.json.GSonUtils;
import org.globsframework.view.model.DictionaryType;
import org.globsframework.view.model.ViewRequestType;

import static org.globsframework.view.ViewEngineImplTest.br;

public class ViewOnArrayTest extends TestCase {


    public void testWihtStringArray() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ObjectWithArray.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("key", breakdowns),
                br("messages", breakdowns)
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

        MutableGlob d1 = ObjectWithArray.TYPE.instantiate()
                .set(ObjectWithArray.key, "n1")
                .set(ObjectWithArray.messages, new String[]{"m1", "m2", "m3"});
        MutableGlob d2 = ObjectWithArray.TYPE.instantiate()
                .set(ObjectWithArray.key, "n1")
                .set(ObjectWithArray.messages, new String[]{"m3", "m4", "m5"});
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ObjectWithArray.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name":"",
                  "nodeName": "root",
                  "__children__":[
                    {
                       "name":"n1",
                       "nodeName":"key",
                       "__children__":[
                          {
                             "name":"[m1, m2, m3]",
                             "nodeName":"messages"
                          },
                          {
                             "name":"[m3, m4, m5]",
                             "nodeName":"messages"
                          }
                       ]
                    }
                  ]
                }"""), GSonUtils.normalize(actual));

    }

    public static class ObjectWithArray {
        public static GlobType TYPE;

        public static StringField key;

        public static StringArrayField messages;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("ObjectWithArray");
            key = builder.declareStringField("key");
            messages = builder.declareStringArrayField("messages");
            TYPE = builder.build();
        }
    }
}
