package org.globsframework.view;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globsframework.http.GlobFile;
import org.globsframework.json.GSonUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.GlobArrayField;
import org.globsframework.metamodel.fields.GlobField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.MutableGlob;
import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.view.filter.Filter;
import org.globsframework.view.filter.model.*;
import org.globsframework.view.model.*;
import org.globsframework.view.server.HttpViewServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ViewEngineImplTest extends TestCase {

    public void testName() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns),
                br("NameSub2", breakdowns),
                br("Name2", breakdowns),
                br("NameSub1", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate()
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
                        .set(ViewOutput.name, "quantity")
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

        String encode = GSonUtils.encode(dictionary, false);
        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n2")
                .set(ViewType1.SUB1, SubType1.TYPE.instantiate().set(SubType1.NameSub1, "subN1"))
                .set(ViewType1.SUB2, new Glob[]{
                        SubType2.TYPE.instantiate().set(SubType2.NameSub2, "sub21").set(SubType2.qty, 1),
                        SubType2.TYPE.instantiate().set(SubType2.NameSub2, "sub22").set(SubType2.qty, 2)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB1, SubType1.TYPE.instantiate().set(SubType1.NameSub1, "subN21"))
                .set(ViewType1.SUB2, new Glob[]{
                        SubType2.TYPE.instantiate().set(SubType2.NameSub2, "sub221").set(SubType2.qty, 3),
                        SubType2.TYPE.instantiate().set(SubType2.NameSub2, "sub222").set(SubType2.qty, 1)});
        View view = viewBuilder.createView();

        View.Accepted accepter = view.getAccepter();
        accepter.enter("Name1");
        Assert.assertTrue(accepter.wanted());
        accepter.leave();
        accepter.enter("Name2");
        Assert.assertTrue(accepter.wanted());
        accepter.leave();

        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"__children__\": [\n" +
                "        {\n" +
                "          \"name\": \"sub21\",\n" +
                "          \"nodeName\": \"SUB2.NameSub2\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"n2\",\n" +
                "              \"nodeName\": \"Name2\",\n" +
                "              \"__children__\": [\n" +
                "                {\n" +
                "                  \"name\": \"subN1\",\n" +
                "                  \"nodeName\": \"SUB1.NameSub1\",\n" +
                "                  \"output\": {\n" +
                "                    \"quantity\": 1.0\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"output\": {\n" +
                "                \"quantity\": 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"output\": {\n" +
                "            \"quantity\": 1.0\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"sub22\",\n" +
                "          \"nodeName\": \"SUB2.NameSub2\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"n2\",\n" +
                "              \"nodeName\": \"Name2\",\n" +
                "              \"__children__\": [\n" +
                "                {\n" +
                "                  \"name\": \"subN1\",\n" +
                "                  \"nodeName\": \"SUB1.NameSub1\",\n" +
                "                  \"output\": {\n" +
                "                    \"quantity\": 2.0\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"output\": {\n" +
                "                \"quantity\": 2.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"output\": {\n" +
                "            \"quantity\": 2.0\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"sub221\",\n" +
                "          \"nodeName\": \"SUB2.NameSub2\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"n22\",\n" +
                "              \"nodeName\": \"Name2\",\n" +
                "              \"__children__\": [\n" +
                "                {\n" +
                "                  \"name\": \"subN21\",\n" +
                "                  \"nodeName\": \"SUB1.NameSub1\",\n" +
                "                  \"output\": {\n" +
                "                    \"quantity\": 3.0\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"output\": {\n" +
                "                \"quantity\": 3.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"output\": {\n" +
                "            \"quantity\": 3.0\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"sub222\",\n" +
                "          \"nodeName\": \"SUB2.NameSub2\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"n22\",\n" +
                "              \"nodeName\": \"Name2\",\n" +
                "              \"__children__\": [\n" +
                "                {\n" +
                "                  \"name\": \"subN21\",\n" +
                "                  \"nodeName\": \"SUB1.NameSub1\",\n" +
                "                  \"output\": {\n" +
                "                    \"quantity\": 1.0\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"output\": {\n" +
                "                \"quantity\": 1.0\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"output\": {\n" +
                "            \"quantity\": 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"output\": {\n" +
                "        \"quantity\": 7.0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"output\": {\n" +
                "    \"quantity\": 7.0\n" +
                "  }\n" +
                "}\n"), GSonUtils.normalize(actual));
    }

    public void testWithSameType() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns),
                br("Name2", breakdowns)
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n2");
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22");
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"__children__\": [\n" +
                "        {\n" +
                "          \"name\": \"n2\",\n" +
                "          \"nodeName\": \"Name2\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"n22\",\n" +
                "          \"nodeName\": \"Name2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}"), GSonUtils.normalize(actual));
    }

    public void testWithOutputWithoutBreakdownOnThisType() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});
        ;
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"output\": {\n" +
                "        \"total\": 3.0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"output\": {\n" +
                "    \"total\": 3.0\n" +
                "  }\n" +
                "}"), GSonUtils.normalize(actual));
    }

    public void testWithOutputWithoutBreakdown() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});
        ;
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"output\": {\n" +
                "    \"total\": 3.0\n" +
                "  }\n" +
                "}"), GSonUtils.normalize(actual));
    }

    public void testWithOutput() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns),
                br("Name2", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});
        ;
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"__children__\": [\n" +
                "        {\n" +
                "          \"name\": \"n11\",\n" +
                "          \"nodeName\": \"Name2\",\n" +
                "          \"output\": {\n" +
                "            \"total\": 1.0\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"n22\",\n" +
                "          \"nodeName\": \"Name2\",\n" +
                "          \"output\": {\n" +
                "            \"total\": 2.0\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"output\": {\n" +
                "        \"total\": 3.0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"output\": {\n" +
                "    \"total\": 3.0\n" +
                "  }\n" +
                "}\n"), GSonUtils.normalize(actual));
    }


    public void testInCsv() throws IOException {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns),
                br("Name2", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});
        ;
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        Glob result = HttpViewServer.dumpInCsv(viewRequest, viewAsGlob, false);

        String str = Files.read(new FileInputStream(result.get(GlobFile.file)), StandardCharsets.UTF_8);

        Assert.assertEquals("Name1;Name2;total\n" +
                ";;3\n" +
                "n1;;3\n" +
                "n1;n11;1\n" +
                "n1;n22;2\n", str);
    }

    public void testInCsvLeafOnly() throws IOException {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns),
                br("Name2", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});
        ;
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        Glob result = HttpViewServer.dumpInCsv(viewRequest, viewAsGlob, true);

        String str = Files.read(new FileInputStream(result.get(GlobFile.file)), StandardCharsets.UTF_8);

        Assert.assertEquals("Name1;Name2;total\n" +
                "n1;n11;1\n" +
                "n1;n22;2\n", str);
    }

    public void testMultilevelWithoutIntermediaryLevel() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns),
                br("NameSub4", breakdowns),
                br("NameSub5", breakdowns)
        });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n2");
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{
                                SubType2.TYPE.instantiate()
                                        .set(SubType2.NameSub2, "A1")
                                        .set(SubType2.NameSub3, "B1")
                                        .set(SubType2.SUB3, new Glob[]{
                                        SubType3.TYPE.instantiate()
                                                .set(SubType3.NameSub4, "s41")
                                                .set(SubType3.NameSub5, "s51"),
                                        SubType3.TYPE.instantiate()
                                                .set(SubType3.NameSub4, "s42")
                                                .set(SubType3.NameSub5, "s52")
                                }),
                                SubType2.TYPE.instantiate()
                                        .set(SubType2.NameSub2, "A2")
                                        .set(SubType2.NameSub3, "B2")
                                        .set(SubType2.SUB3, new Glob[]{
                                        SubType3.TYPE.instantiate()
                                                .set(SubType3.NameSub4, "s6"),
                                        SubType3.TYPE.instantiate()
                                                .set(SubType3.NameSub4, "s7")})
                        }
                );
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"__children__\": [\n" +
                "        {\n" +
                "          \"name\": \"null\",\n" +
                "          \"nodeName\": \"SUB2.SUB3.NameSub4\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"null\",\n" +
                "              \"nodeName\": \"SUB2.SUB3.NameSub5\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"s41\",\n" +
                "          \"nodeName\": \"SUB2.SUB3.NameSub4\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"s51\",\n" +
                "              \"nodeName\": \"SUB2.SUB3.NameSub5\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"s42\",\n" +
                "          \"nodeName\": \"SUB2.SUB3.NameSub4\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"s52\",\n" +
                "              \"nodeName\": \"SUB2.SUB3.NameSub5\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"s6\",\n" +
                "          \"nodeName\": \"SUB2.SUB3.NameSub4\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"null\",\n" +
                "              \"nodeName\": \"SUB2.SUB3.NameSub5\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"s7\",\n" +
                "          \"nodeName\": \"SUB2.SUB3.NameSub4\",\n" +
                "          \"__children__\": [\n" +
                "            {\n" +
                "              \"name\": \"null\",\n" +
                "              \"nodeName\": \"SUB2.SUB3.NameSub5\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n"), GSonUtils.normalize(actual));
    }

    public void testWithConvertStringToDouble() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns)
        })
                .set(ViewRequestType.output, new Glob[]{
                        ViewOutput.TYPE.instantiate().set(ViewOutput.name, "strValue")
                        .set(ViewOutput.uniqueName, br("strValue", breakdowns).get(ViewBreakdown.uniqueName))
                });
        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);


        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.strValue, "11");
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.strValue, "44");
        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"output\": {\n" +
                "        \"strValue\": 55.0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"output\": {\n" +
                "    \"strValue\": 55.0\n" +
                "  }\n" +
                "}"), GSonUtils.normalize(actual));
    }

    public void testFilter() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });

        viewRequest.set(ViewRequestType.filter, FilterType.TYPE.instantiate()
        .set(FilterType.filter,
                AndFilterType.TYPE.instantiate()
                                .set(AndFilterType.filters, new Glob[]{
                                        EqualType.TYPE.instantiate()
                                                .set(EqualType.uniqueName, br("Name2", breakdowns).get(ViewBreakdown.uniqueName))
                                                .set(EqualType.value, "n11"),
                                        GreaterOrEqualType.TYPE.instantiate()
                                                .set(GreaterOrEqualType.uniqueName,
                                                        br("Name1", breakdowns).get(ViewBreakdown.uniqueName))
                                                .set(GreaterOrEqualType.value, "a")
                                })
        ));

        System.out.println(GSonUtils.encode(viewRequest, true));

        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n2")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});

        View view = viewBuilder.createView();
        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n1\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"output\": {\n" +
                "        \"total\": 1.0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"output\": {\n" +
                "    \"total\": 1.0\n" +
                "  }\n" +
                "}"), GSonUtils.normalize(actual));
    }


    public void testIndexFilter() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });

        viewRequest.set(ViewRequestType.filter, FilterType.TYPE.instantiate()
                .set(FilterType.filter,
                        AndFilterType.TYPE.instantiate()
                                .set(AndFilterType.filters, new Glob[]{
                                        EqualType.TYPE.instantiate()
                                                .set(EqualType.uniqueName, br("NameSub2", breakdowns).get(ViewBreakdown.uniqueName))
                                                .set(EqualType.value, "sub2")
                                })
                ));

        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate()
                        .set(SubType2.NameSub2, "sub1")
                        .set(SubType2.qty, 1)});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n2")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate()
                        .set(SubType2.NameSub2, "sub2")
                        .set(SubType2.qty, 2)});
        MutableGlob d3 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n3")
                .set(ViewType1.Name2, "n33")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate()
                        .set(SubType2.NameSub2, "sub2")
                        .set(SubType2.qty, 1)});

        View view = viewBuilder.createView();

        final Filter indexFilter = view.getIndexFilter(IndexViewType1.TYPE, uniqueName -> {
            if (uniqueName.equals("SUB2.NameSub2")) {
                return new UniqueNameToPath.PathField(new String[0], "idx1");
            } else {
                throw new ItemNotFound(uniqueName);
            }
        });

        View.Append appender = view.getAppender(ViewType1.TYPE);
        if (isFiltered(d1, indexFilter)) appender.add(d1);
        if (isFiltered(d2, indexFilter)) appender.add(d2);
        if (isFiltered(d3, indexFilter)) appender.add(d3);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n2",
                      "nodeName": "Name1",
                      "output": {
                        "total": 2.0
                      }
                    },
                    {
                      "name": "n3",
                      "nodeName": "Name1",
                      "output": {
                        "total": 1.0
                      }
                    }
                  ],
                  "output": {
                    "total": 3.0
                  }
                }"""), GSonUtils.normalize(actual));
    }

    private static boolean isFiltered(MutableGlob d1, Filter indexFilter) {
        return indexFilter.isFiltered(IndexViewType1.TYPE.instantiate().set(IndexViewType1.idx1,
                Arrays.stream(d1.getOrEmpty(ViewType1.SUB2))
                        .map(SubType2.NameSub2).findFirst().orElse(null)));
    }

    public static class IndexViewType1 {
        public static GlobType TYPE;

        public static StringField idx1;

        static {
            GlobTypeLoaderFactory.create(IndexViewType1.class).load();
        }
    }



    public void testFilterOnArrayOfGlob() {
        ViewEngine viewEngine = new ViewEngineImpl();

        Glob dictionary = viewEngine.createDictionary(ViewType1.TYPE);

        MutableGlob viewRequest = ViewRequestType.TYPE.instantiate();
        Glob[] breakdowns = dictionary.get(DictionaryType.breakdowns);
        viewRequest.set(ViewRequestType.breakdowns, new Glob[]{
                br("Name1", breakdowns)
        });
        viewRequest.set(ViewRequestType.output, new Glob[]{
                ViewOutput.TYPE.instantiate().set(ViewOutput.name, "total")
                        .set(ViewOutput.uniqueName, br("qty", breakdowns).get(ViewBreakdown.uniqueName))
        });

        viewRequest.set(ViewRequestType.filter, FilterType.TYPE.instantiate()
                .set(FilterType.filter,
                        AndFilterType.TYPE.instantiate()
                                .set(AndFilterType.filters, new Glob[]{
                                        EqualType.TYPE.instantiate()
                                                .set(EqualType.uniqueName, br("NameSub2", breakdowns).get(ViewBreakdown.uniqueName))
                                                .set(EqualType.value, "bad name")
                                })
                ));

        System.out.println(GSonUtils.encode(viewRequest, true));

        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);

//        String encode = GSonUtils.encode(dictionary, false);
//        System.out.println(encode);

        MutableGlob d1 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n1")
                .set(ViewType1.Name2, "n11")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.NameSub2, "good name")});
        MutableGlob d2 = ViewType1.TYPE.instantiate()
                .set(ViewType1.Name1, "n2")
                .set(ViewType1.Name2, "n22")
                .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.NameSub2, "bad name")});

        View view = viewBuilder.createView();

        View.Accepted accepter = view.getAccepter();

        accepter.enter(ViewType1.Name1.getName());
        Assert.assertTrue(accepter.wanted());
        accepter.leave();

        accepter.enter(ViewType1.Name2.getName());
        Assert.assertFalse(accepter.wanted());
        accepter.leave();

        accepter.enter(ViewType1.SUB2.getName());
        Assert.assertTrue(accepter.wanted());

        accepter.enter(SubType2.NameSub3.getName());
        Assert.assertFalse(accepter.wanted());
        accepter.leave();

        accepter.enter(SubType2.NameSub2.getName());
        Assert.assertTrue(accepter.wanted());
        accepter.leave();

        accepter.leave();

        View.Append appender = view.getAppender(ViewType1.TYPE);
        appender.add(d1);
        appender.add(d2);
        appender.complete();
        Glob viewAsGlob = view.toGlob();
        String actual = GSonUtils.encode(viewAsGlob, false);

        Assert.assertEquals(GSonUtils.normalize("{\n" +
                "  \"name\": \"\",\n" +
                "  \"nodeName\": \"root\",\n" +
                "  \"__children__\": [\n" +
                "    {\n" +
                "      \"name\": \"n2\",\n" +
                "      \"nodeName\": \"Name1\",\n" +
                "      \"output\": {\n" +
                "        \"total\": 0.0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"output\": {\n" +
                "    \"total\": 0.0\n" +
                "  }\n" +
                "}"), GSonUtils.normalize(actual));
    }

    public static Glob br(String name, Glob[] breakdowns) {
        for (Glob breakdown : breakdowns) {
            if (breakdown.get(SimpleBreakdown.fieldName).equals(name)) {
                return ViewBreakdown.TYPE.instantiate().set(ViewBreakdown.uniqueName,
                        breakdown.get(SimpleBreakdown.uniqueName));
            }
        }
        throw new RuntimeException(name + " not found in " + Arrays.stream(breakdowns).map(glob -> glob.get(SimpleBreakdown.fieldName)).collect(Collectors.joining(", ")));
    }

    static public class ViewType1 {
        public static GlobType TYPE;
        public static StringField Name1;
        public static StringField Name2;
        @Target(SubType1.class)
        public static GlobField SUB1;
        @Target(SubType2.class)
        public static GlobArrayField SUB2;

        @_StringAsDouble
        public static StringField strValue;

        static {
            GlobTypeLoaderFactory.create(ViewType1.class).load();
        }
    }

    static public class SubType1 {
        public static GlobType TYPE;

        public static StringField NameSub1;

        static {
            GlobTypeLoaderFactory.create(SubType1.class).load();
        }
    }

    static public class SubType2 {
        public static GlobType TYPE;

        public static StringField NameSub2;

        public static StringField NameSub3;

        public static DoubleField qty;

        @Target(SubType3.class)
        public static GlobArrayField SUB3;

        static {
            GlobTypeLoaderFactory.create(SubType2.class).load();
        }
    }

    static public class SubType3 {
        public static GlobType TYPE;

        public static StringField NameSub4;

        public static StringField NameSub5;

        static {
            GlobTypeLoaderFactory.create(SubType3.class).load();
        }
    }
}