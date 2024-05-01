With this library, is is possible to query Globs using breakdown and output.
It is used to expose an endpoint to output a dictionnary and a view (hierarchical tree or flat like a csv)

Programatiacly:
```
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
```

Given a list a ViewType1 glob it will produce
```
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
....
```
