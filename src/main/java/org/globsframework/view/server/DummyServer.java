package org.globsframework.view.server;

import com.google.gson.Gson;
import org.globsframework.commandline.ParseCommandLine;
import org.globsframework.core.metamodel.GlobModel;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.GlobTypeResolver;
import org.globsframework.core.metamodel.annotations.AllCoreAnnotations;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobModel;
import org.globsframework.core.model.Glob;
import org.globsframework.json.GlobTypeSet;
import org.globsframework.json.GlobsGson;
import org.globsframework.view.View;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


/*

curl http://localhost:2000/sources

curl http://localhost:2000/dictionary?source=dummySource

cat << EOF |
{
  "breakdowns": [
    {
      "typeName":"dummyType",
      "fieldName": "n2",
      "nativeType": "String"
    },
    {
       "typeName":"dummySubType",
      "fieldName": "s2",
      "nativeType": "String"
   }],
   "output": [
    {
      "typeName":"dummySubType",
      "fieldName": "qty",
      "nativeType": "double"
    }
   ]
}
EOF
 curl -X POST --data @- -H "Content-Type: text/json" http://localhost:2000/computeView?source=dummySource

 */

public class DummyServer implements DataAccessor {
    private final HttpViewServer httpViewServer;
    private final GlobType rootType;
    private final Glob[] data;
    private Source source;

    public DummyServer(Glob httpOption, Glob dummyServerOptions) throws InterruptedException, IOException {
        httpViewServer = new HttpViewServer(httpOption, this);
        Gson gson = GlobsGson.create(GlobTypeResolver.chain(AllCoreAnnotations.MODEL::getType));
        String fileName = dummyServerOptions.get(Options.typeFileName);
        Reader reader;
        if (fileName == null) {
            reader = new InputStreamReader(getClass().getResourceAsStream("/SimpleDummyType.json"), StandardCharsets.UTF_8);
        } else {
            reader = new FileReader(fileName);
        }
        GlobTypeSet globTypes = gson.fromJson(reader, GlobTypeSet.class);
        reader.close();

        GlobModel globModel = new DefaultGlobModel(globTypes.globType);

        Gson gsonData = GlobsGson.create(GlobTypeResolver.chain(AllCoreAnnotations.MODEL::findType, globModel::findType));
        String dataFileName = dummyServerOptions.get(Options.dataFileName);
        Reader dataReader;
        if (dataFileName == null) {
            dataReader = new InputStreamReader(getClass().getResourceAsStream("/SimpleDummyData.json"), StandardCharsets.UTF_8);
        } else {
            dataReader = new FileReader(dataFileName);
        }
        data = gsonData.fromJson(dataReader, Glob[].class);
        dataReader.close();
        if (data.length == 0) {
            throw new RuntimeException("No data read");
        }
        rootType = data[0].getType();

        source = new Source() {
            public String getID() {
                return "dummySource";
            }

            public GlobType getOutputType() {
                return rootType;
            }

            public DataConsumer create(View.Accepted accepted) {
                return new DataConsumer() {
                    public GlobType getOutputType() {
                        return getOutputType();
                    }

                    public void getAll(Consumer<Glob> consumer) {
                        for (Glob d : data) {
                            consumer.accept(d);
                        }
                    }
                };
            }
        };
    }

    public List<Glob> getSources() {
        return List.of(SourceNameType.TYPE.instantiate()
                .set(SourceNameType.ID, source.getID())
                .set(SourceNameType.NAME, new String[]{source.getID()}));
    }

    public Source getSource(String source) {
        if (!source.equals(this.source.getID())) {
            throw new RuntimeException("Unknown source " + source + " got " + this.source.getID());
        }
        return this.source;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Glob httpOptions = ParseCommandLine.parse(HttpViewServer.Options.TYPE, argsList, true);
        Glob serverOptions = ParseCommandLine.parse(DummyServer.Options.TYPE, argsList, false);
        DummyServer dummyServer = new DummyServer(httpOptions, serverOptions);
        dummyServer.run();
    }

    private void run() {
    }

    public static class Options {
        public static GlobType TYPE;

        public static StringField typeFileName;

        public static StringField dataFileName;

        static {
            GlobTypeLoaderFactory.create(Options.class).load();
        }
    }


}
