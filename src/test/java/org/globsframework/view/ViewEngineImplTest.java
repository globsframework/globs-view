package org.globsframework.view;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.utils.Files;
import org.globsframework.core.utils.exceptions.ItemNotFound;
import org.globsframework.http.GlobFile;
import org.globsframework.json.GSonUtils;
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "__children__": [
                        {
                          "name": "sub21",
                          "nodeName": "SUB2.NameSub2",
                          "__children__": [
                            {
                              "name": "n2",
                              "nodeName": "Name2",
                              "__children__": [
                                {
                                  "name": "subN1",
                                  "nodeName": "SUB1.NameSub1",
                                  "output": {
                                    "quantity": 1.0
                                  }
                                }
                              ],
                              "output": {
                                "quantity": 1.0
                              }
                            }
                          ],
                          "output": {
                            "quantity": 1.0
                          }
                        },
                        {
                          "name": "sub22",
                          "nodeName": "SUB2.NameSub2",
                          "__children__": [
                            {
                              "name": "n2",
                              "nodeName": "Name2",
                              "__children__": [
                                {
                                  "name": "subN1",
                                  "nodeName": "SUB1.NameSub1",
                                  "output": {
                                    "quantity": 2.0
                                  }
                                }
                              ],
                              "output": {
                                "quantity": 2.0
                              }
                            }
                          ],
                          "output": {
                            "quantity": 2.0
                          }
                        },
                        {
                          "name": "sub221",
                          "nodeName": "SUB2.NameSub2",
                          "__children__": [
                            {
                              "name": "n22",
                              "nodeName": "Name2",
                              "__children__": [
                                {
                                  "name": "subN21",
                                  "nodeName": "SUB1.NameSub1",
                                  "output": {
                                    "quantity": 3.0
                                  }
                                }
                              ],
                              "output": {
                                "quantity": 3.0
                              }
                            }
                          ],
                          "output": {
                            "quantity": 3.0
                          }
                        },
                        {
                          "name": "sub222",
                          "nodeName": "SUB2.NameSub2",
                          "__children__": [
                            {
                              "name": "n22",
                              "nodeName": "Name2",
                              "__children__": [
                                {
                                  "name": "subN21",
                                  "nodeName": "SUB1.NameSub1",
                                  "output": {
                                    "quantity": 1.0
                                  }
                                }
                              ],
                              "output": {
                                "quantity": 1.0
                              }
                            }
                          ],
                          "output": {
                            "quantity": 1.0
                          }
                        }
                      ],
                      "output": {
                        "quantity": 7.0
                      }
                    }
                  ],
                  "output": {
                    "quantity": 7.0
                  }
                }
                """), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "__children__": [
                        {
                          "name": "n2",
                          "nodeName": "Name2"
                        },
                        {
                          "name": "n22",
                          "nodeName": "Name2"
                        }
                      ]
                    }
                  ]
                }"""), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "output": {
                        "total": 3.0
                      }
                    }
                  ],
                  "output": {
                    "total": 3.0
                  }
                }"""), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "output": {
                    "total": 3.0
                  }
                }"""), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "__children__": [
                        {
                          "name": "n11",
                          "nodeName": "Name2",
                          "output": {
                            "total": 1.0
                          }
                        },
                        {
                          "name": "n22",
                          "nodeName": "Name2",
                          "output": {
                            "total": 2.0
                          }
                        }
                      ],
                      "output": {
                        "total": 3.0
                      }
                    }
                  ],
                  "output": {
                    "total": 3.0
                  }
                }
                """), GSonUtils.normalize(actual));
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
        {
            View.Append appender = view.getAppender(ViewType1.TYPE);
            appender.add(d1);
            appender.add(d2);
            appender.complete();
            Glob viewAsGlob = view.toGlob();
            Glob result = HttpViewServer.dumpInCsv(viewRequest, viewAsGlob, false);

            String str = Files.read(new FileInputStream(result.get(GlobFile.file)), StandardCharsets.UTF_8);

            Assert.assertEquals("""
                    Name1;Name2;total
                    ;;3
                    n1;;3
                    n1;n11;1
                    n1;n22;2
                    """, str);
        }
        {
            view.reset();
            MutableGlob d3 = ViewType1.TYPE.instantiate()
                    .set(ViewType1.Name1, "n2")
                    .set(ViewType1.Name2, "n22")
                    .set(ViewType1.SUB2, new Glob[]{SubType2.TYPE.instantiate().set(SubType2.qty, 2)});
            View.Append appender = view.getAppender(ViewType1.TYPE);
            appender.add(d3);
            appender.complete();
            Glob viewAsGlob = view.toGlob();
            Glob result = HttpViewServer.dumpInCsv(viewRequest, viewAsGlob, false);

            String str = Files.read(new FileInputStream(result.get(GlobFile.file)), StandardCharsets.UTF_8);

            Assert.assertEquals("""
                    Name1;Name2;total
                    ;;2
                    n2;;2
                    n2;n22;2
                    """, str);

        }
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

        Assert.assertEquals("""
                Name1;Name2;total
                n1;n11;1
                n1;n22;2
                """, str);
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "__children__": [
                        {
                          "name": "",
                          "nodeName": "SUB2.SUB3.NameSub4",
                          "__children__": [
                            {
                              "name": "",
                              "nodeName": "SUB2.SUB3.NameSub5"
                            }
                          ]
                        },
                        {
                          "name": "s41",
                          "nodeName": "SUB2.SUB3.NameSub4",
                          "__children__": [
                            {
                              "name": "s51",
                              "nodeName": "SUB2.SUB3.NameSub5"
                            }
                          ]
                        },
                        {
                          "name": "s42",
                          "nodeName": "SUB2.SUB3.NameSub4",
                          "__children__": [
                            {
                              "name": "s52",
                              "nodeName": "SUB2.SUB3.NameSub5"
                            }
                          ]
                        },
                        {
                          "name": "s6",
                          "nodeName": "SUB2.SUB3.NameSub4",
                          "__children__": [
                            {
                              "name": "",
                              "nodeName": "SUB2.SUB3.NameSub5"
                            }
                          ]
                        },
                        {
                          "name": "s7",
                          "nodeName": "SUB2.SUB3.NameSub4",
                          "__children__": [
                            {
                              "name": "",
                              "nodeName": "SUB2.SUB3.NameSub5"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "output": {
                        "strValue": 55.0
                      }
                    }
                  ],
                  "output": {
                    "strValue": 55.0
                  }
                }"""), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n1",
                      "nodeName": "Name1",
                      "output": {
                        "total": 1.0
                      }
                    }
                  ],
                  "output": {
                    "total": 1.0
                  }
                }"""), GSonUtils.normalize(actual));
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

        Assert.assertEquals(GSonUtils.normalize("""
                {
                  "name": "",
                  "nodeName": "root",
                  "__children__": [
                    {
                      "name": "n2",
                      "nodeName": "Name1",
                      "output": {
                        "total": 0.0
                      }
                    }
                  ],
                  "output": {
                    "total": 0.0
                  }
                }"""), GSonUtils.normalize(actual));
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
