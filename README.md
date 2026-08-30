# Globs View

Query a collection of [Glob](https://globsframework.org)s by **breakdown and output** — the pivot-table
shape: group the data by a series of dimensions, aggregate a few measures, and get back a hierarchical tree
(or a flat CSV export). The dimensions are not written in code: they are discovered from the `GlobType`,
published as a *dictionary*, and picked by name in the request. That is what makes it an endpoint an
interactive UI can drive.

## Requirements

Java 21, `org.globsframework:globs`, `globs-gson`; `globs-http` for the ready-made server.

## Installation

```xml
<dependency>
    <groupId>org.globsframework</groupId>
    <artifactId>globs-view</artifactId>
    <version>5.1.4</version>
</dependency>
```

## The three steps

1. **The dictionary** — `createDictionary(type)` walks the `GlobType`, descending into `GlobField`s and
   `GlobArrayField`s, and returns a Glob listing every dimension it found: a `uniqueName` (the one the
   request uses), the `path` to reach it, its type, and whether it is flagged `IsSensibleData`.
2. **The request** — a `ViewRequestType` Glob: the ordered `breakdowns` (the nesting of the result tree),
   the `output` measures, and an optional `filter`.
3. **The view** — `buildView(dictionary, request)` returns a `ViewBuilder`; `createView()` gives a `View`
   you feed Globs into, and `toGlob()` renders the tree.

```java
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

View view = viewBuilder.createView();
View.Append appender = view.getAppender(ViewType1.TYPE);
globs.forEach(appender::add);
appender.complete();
Glob result = view.toGlob();
```

Given a list of `ViewType1` globs, the result is the breakdown tree, each node carrying its aggregated
outputs:

```json
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
                  "output": { "quantity": 1.0 }
                }
              ],
              "output": { "quantity": 1.0 }
            }
          ],
          "output": { "quantity": 1.0 }
        }
      ]
    }
  ]
}
```

`View.getAccepter()` answers, per dimension name, whether the current request wants it — so a data source
can skip loading a column nobody asked for, before any Glob is built. A view that would expand past
`globs.view.max.node` (500 000 by default) raises `TooManyNodeException` rather than filling memory.

## Filters

`ViewRequestType.filter` holds a filter tree, again as Globs: `EqualType`, `NotEqualType`, `ContainsType` /
`NotContainsType`, `StrictlyGreaterType` / `GreaterOrEqualType`, `StrictlyLessType` / `LessOrEqualType`,
`IsNullType` / `IsNotNullType`, combined with `AndFilterType`, `OrFilterType` and `NotType`, each pointing at
a dimension through a `PathToField`. Being Globs, a filter arrives as JSON from a client, and `FilterBuilder`
compiles it once into a `Filter`.

## Serving it over HTTP

`HttpViewServer` wires the whole thing onto `globs-http`, with the OpenAPI description included:

| Route | What it does |
| --- | --- |
| `GET /sources` | the data sources a `DataAccessor` exposes |
| `GET /dictionary` | the dimensions available on a source |
| `POST /computeView` | takes a `ViewRequest`, returns the tree — or a CSV file with `outputType=csv` (`leafOnly` for the leaves only) |

```java
new HttpViewServer("0.0.0.0", 8080, dataAccessor);
```

Implement `DataAccessor` / `Source` to plug in where the Globs come from; `CsvExporter` is the flat rendering
used by the `csv` output type, and `DummyServer` is a runnable example.

Fields annotated `IsSensibleData` are flagged in the dictionary so a server can refuse to break down on
them; `StringAsDouble` lets a string dimension be aggregated as a number.

## Building

```bash
mvn -o test
```

## License

Apache License 2.0 — see <https://www.apache.org/licenses/LICENSE-2.0.txt>.

## Links

- [Globs Framework](https://globsframework.org)
- [GitHub repository](https://github.com/globsframework/globs-view)
