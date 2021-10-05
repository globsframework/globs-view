package org.globsframework.view.server;

import org.apache.commons.io.FileUtils;
import org.apache.http.ExceptionLogger;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.globsframework.export.ExportBySize;
import org.globsframework.http.GlobFile;
import org.globsframework.http.HttpServerRegister;
import org.globsframework.http.HttpTreatment;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeBuilder;
import org.globsframework.metamodel.GlobTypeLoaderFactory;
import org.globsframework.metamodel.annotations.Comment_;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.MutableGlob;
import org.globsframework.utils.collections.Pair;
import org.globsframework.view.*;
import org.globsframework.view.model.ViewBreakdown;
import org.globsframework.view.model.ViewOutput;
import org.globsframework.view.model.ViewRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpViewServer {
    public static final int EXPIRATION = 1000 * 60 * 20; // => cache 20 min
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpViewServer.class);
    private final Glob options;
    private final DataAccessor dataAccessor;
    private final int port;
    private final org.apache.http.impl.nio.bootstrap.HttpServer httpServer;
    private final Map<String, Pair<Long, Source>> sourceCache = new ConcurrentHashMap<>();

    public HttpViewServer(Glob options, DataAccessor dataAccessor) throws InterruptedException, IOException {
        this.options = options;
        this.dataAccessor = dataAccessor;

        final IOReactorConfig config = IOReactorConfig.custom()
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .build();
        ServerBootstrap serverBootstrap = ServerBootstrap.bootstrap()
                .setListenerPort(options.get(Options.httpPort, 0))
                .setIOReactorConfig(config)
                .setExceptionLogger(new StdErrorExceptionLogger(LOGGER));

        HttpServerRegister httpServerRegister = new HttpServerRegister("viewServer/1.1");
        httpServerRegister.registerOpenApi();
        httpServerRegister.register("/sources", null)
                .get(null, new HttpTreatment() {
                    public CompletableFuture<Glob> consume(Glob body, Glob url, Glob queryParameters) {
                        return CompletableFuture.completedFuture(SourcesType.TYPE.instantiate().set(SourcesType.sources,
                                dataAccessor.getSources().toArray(Glob[]::new)
                        ));
                    }
                });

        httpServerRegister.register("/dictionary", null)
                .get(ParamType.TYPE, new HttpTreatment() {
                    public CompletableFuture<Glob> consume(Glob body, Glob url, Glob queryParameters) throws Exception {
                        String source = queryParameters.getNotNull(ParamType.source);
                        Pair<Long, Source> longGlobPair = sourceCache.get(source);
                        Source src;
                        if (longGlobPair != null && System.currentTimeMillis() < longGlobPair.getFirst()) {
                            src = longGlobPair.getSecond();
                        } else {
                            src = dataAccessor.getSource(source);
                            sourceCache.put(source, Pair.makePair(System.currentTimeMillis() + EXPIRATION, src));
                        }
                        ViewEngine viewEngine = new ViewEngineImpl();
                        Glob dictionary = viewEngine.createDictionary(src.getOutputType());
                        return CompletableFuture.completedFuture(dictionary);
                    }
                });

        httpServerRegister.register("/computeView", null)
//                .setGzipCompress()
                .post(ViewRequestType.TYPE, ParamType.TYPE, new HttpTreatment() {
                    public CompletableFuture<Glob> consume(Glob viewRequest, Glob url, Glob queryParameters) throws Exception {
                        String source = queryParameters.getNotNull(ParamType.source);
                        ViewEngine viewEngine = new ViewEngineImpl();
                        Pair<Long, Source> longGlobPair = sourceCache.get(source);
                        Source src;
                        if (longGlobPair != null && System.currentTimeMillis() < longGlobPair.getFirst()) {
                            src = longGlobPair.getSecond();
                        } else {
                            src = dataAccessor.getSource(source);
                            sourceCache.put(source, Pair.makePair(System.currentTimeMillis() + EXPIRATION, src));
                        }
                        Glob dictionary = viewEngine.createDictionary(src.getOutputType());
                        ViewBuilder viewBuilder = viewEngine.buildView(dictionary, viewRequest);
                        View view = viewBuilder.createView();
                        Source optimizedSrc = src; //dataAccessor.getSource(source);
                        Source.DataConsumer dataConsumer = optimizedSrc.create(view.getAccepter());
                        View.Append appender = view.getAppender(dataConsumer.getOutputType());
                        dataConsumer.getAll(appender::add);
                        appender.complete();
                        Glob value = view.toGlob();
                        if (queryParameters.get(ParamType.outputType, "json").equals("csv")) {
                            return CompletableFuture.completedFuture(dumpInCsv(viewRequest, value, queryParameters.get(ParamType.leafOnly, false)));
                        }
                        return CompletableFuture.completedFuture(value);
                    }
                });

        Pair<HttpServer, Integer> httpServerIntegerPair = httpServerRegister.startAndWaitForStartup(serverBootstrap);
        httpServer = httpServerIntegerPair.getFirst();
        port = httpServerIntegerPair.getSecond();
        System.out.println("PORT: " + port);
        LOGGER.info("http port : " + port);
    }

    public int getPort() {
        return port;
    }

    public void end() {
        httpServer.shutdown(1, TimeUnit.SECONDS);
    }

    public void waitEnd() throws InterruptedException {
        httpServer.awaitTermination(10, TimeUnit.SECONDS);
    }

    public static Glob dumpInCsv(Glob request, Glob root, boolean leafOnly) throws IOException {
        Glob[] breakdowns = request.getOrEmpty(ViewRequestType.breakdowns);
        Glob[] output = request.getOrEmpty(ViewRequestType.output);
        GlobTypeBuilder globTypeBuilder = new DefaultGlobTypeBuilder("CSV");
        StringField[] breakdownFields = new StringField[breakdowns.length];
        for (int i = 0; i < breakdowns.length; i++) {
            Glob breakdown = breakdowns[i];
            breakdownFields[i] = globTypeBuilder.declareStringField(breakdown.get(ViewBreakdown.aliasName, breakdown.get(ViewBreakdown.uniqueName)));
        }
        GlobType viewType = root.getType();
        GlobField outputField = (GlobField) viewType.getField(ViewBuilderImpl.OUTPUT);
        GlobType outputType = outputField.getTargetType();
        List<Pair<Field, Field>> copy = new ArrayList<>();
        for (Glob o : output) {
            Field field = outputType.getField(o.get(ViewOutput.name));
            Field targetCsvOutput = globTypeBuilder.declare(field.getName(), field.getDataType(), Collections.emptyList());
            copy.add(Pair.makePair(field, targetCsvOutput));
        }
        Path content = Files.createTempFile("viewContent", ".csv");
        File file = content.toFile();
        FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
        Writer writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
        ExportBySize exportBySize = new ExportBySize();
        exportBySize.withSeparator(';');
        GlobType csvOutput = globTypeBuilder.get();
        exportBySize.exportHeader(csvOutput, writer);
        Consumer<Glob> export = exportBySize.export(writer);
        CsvExporter csvExporter = new CsvExporter(export, breakdownFields, copy, outputField,
                viewType.getField(ViewBuilderImpl.NAME).asStringField(),
                (GlobArrayField) viewType.getField(ViewBuilderImpl.CHILD_FIELD_NAME), leafOnly);
        csvExporter.scan(csvOutput.instantiate(), root, -1);
        writer.close();
        return GlobFile.TYPE.instantiate()
                .set(GlobFile.removeWhenDelivered, true)
                .set(GlobFile.mimeType, "text/csv")
                .set(GlobFile.file, file.getAbsolutePath());

    }

    static class CsvExporter {
        final Consumer<Glob> export;
        final StringField[] breakdownFields;
        final List<Pair<Field, Field>> copy;
        final GlobField outputField;
        final StringField breakdownField;
        final GlobArrayField children;
        final boolean leafOnly;

        CsvExporter(Consumer<Glob> export, StringField[] breakdownFields, List<Pair<Field, Field>> copy, GlobField outputField, StringField breakdownField, GlobArrayField children, boolean leafOnly) {
            this.export = export;
            this.breakdownFields = breakdownFields;
            this.copy = copy;
            this.outputField = outputField;
            this.breakdownField = breakdownField;
            this.children = children;
            this.leafOnly = leafOnly;
        }

        private void scan(MutableGlob current, Glob node, int level) {
            MutableGlob sub = current.duplicate();
            if (level >= 0) {
                sub.setValue(breakdownFields[level], node.getValue(breakdownField));
            }
            if (!leafOnly || breakdownFields.length == level + 1) {
                Glob output = node.get(outputField);
                if (output != null) {
                    for (Pair<Field, Field> fieldFieldPair : copy) {
                        sub.setValue(fieldFieldPair.getSecond(), output.getValue(fieldFieldPair.getFirst()));
                    }
                } else {
                    for (Pair<Field, Field> fieldFieldPair : copy) {
                        sub.setValue(fieldFieldPair.getSecond(), null);
                    }
                }
                export.accept(sub);
            }
            for (Glob glob : node.getOrEmpty(children)) {
                scan(sub, glob, level + 1);
            }
        }
    }

    static public class ParamType {
        public static GlobType TYPE;

        public static StringField source;

        //json or csv
        @Comment_("json or csv")
        public static StringField outputType;

        public static BooleanField leafOnly;

        static {
            GlobTypeLoaderFactory.create(ParamType.class).load();
        }
    }

    static public class SourcesType {
        public static GlobType TYPE;

        @Target(SourceNameType.class)
        public static GlobArrayField sources;

        static {
            GlobTypeLoaderFactory.create(SourcesType.class).load();
        }
    }


    static public class Options {
        public static GlobType TYPE;

        public static IntegerField httpPort;

        static {
            GlobTypeLoaderFactory.create(Options.class).load();
        }
    }

    private class StdErrorExceptionLogger implements ExceptionLogger {
        private Logger logger;

        public StdErrorExceptionLogger(Logger logger) {
            this.logger = logger;
        }

        public void log(Exception ex) {
            logger.error("on http layer", ex);

        }
    }
}
