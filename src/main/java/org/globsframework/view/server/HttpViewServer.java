package org.globsframework.view.server;

import org.apache.commons.io.FileUtils;
import org.apache.http.ExceptionLogger;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeLoaderFactory;
import org.globsframework.core.metamodel.annotations.Comment_;
import org.globsframework.core.metamodel.annotations.Target;
import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.utils.Strings;
import org.globsframework.core.utils.collections.Pair;
import org.globsframework.csv.ExportBySize;
import org.globsframework.http.GlobFile;
import org.globsframework.http.HttpServerRegister;
import org.globsframework.http.HttpTreatment;
import org.globsframework.view.*;
import org.globsframework.view.filter.Filter;
import org.globsframework.view.model.ViewRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HttpViewServer {
    public static final int EXPIRATION = 1000 * 60 * 20; // => cache 20 min
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpViewServer.class);
    private final DataAccessor dataAccessor;
    private final int port;
    private final org.apache.http.impl.nio.bootstrap.HttpServer httpServer;
    private final Map<String, Pair<Long, Source>> sourceCache = new ConcurrentHashMap<>();

    public HttpViewServer(Glob options, DataAccessor dataAccessor) throws InterruptedException, IOException {
        this(options.get(Options.localViewAddress), options.get(Options.httpPort, 0), dataAccessor);
    }

    public HttpViewServer(String localAddress, int httpPort, DataAccessor dataAccessor) throws InterruptedException, IOException {
        this.dataAccessor = dataAccessor;

        final IOReactorConfig config = IOReactorConfig.custom()
                .setSoReuseAddress(true)
                .build();
        ServerBootstrap bootstrap = ServerBootstrap.bootstrap();
        if (Strings.isNotEmpty(localAddress)) {
            bootstrap.setLocalAddress(InetAddress.getByName(localAddress));
        }
        ServerBootstrap serverBootstrap = bootstrap
                .setListenerPort(httpPort)
                .setIOReactorConfig(config)
                .setExceptionLogger(new StdErrorExceptionLogger(LOGGER));

        HttpServerRegister httpServerRegister = new HttpServerRegister("viewServer/1.1");
        httpServerRegister.registerOpenApi();
        httpServerRegister.register("/sources", null)
                .get(null, new HttpTreatment() {
                    public CompletableFuture<Glob> consume(Glob body, Glob pathParameters, Glob queryParameters) {
                        return CompletableFuture.completedFuture(SourcesType.TYPE.instantiate().set(SourcesType.sources,
                                dataAccessor.getSources().toArray(Glob[]::new)
                        ));
                    }
                });

        httpServerRegister.register("/dictionary", null)
                .get(ParamType.TYPE, new HttpTreatment() {
                    public CompletableFuture<Glob> consume(Glob body, Glob pathParameters, Glob queryParameters) throws Exception {
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
                    public CompletableFuture<Glob> consume(Glob viewRequest, Glob pathParameters, Glob queryParameters) throws Exception {
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
                        final GlobType index = dataConsumer.getIndex();
                        Filter filter;
                        if (index != null) {
                            final Source.IndexFieldRemap indexFieldRemap = dataConsumer.getIndexRemap();
                            filter = view.getIndexFilter(index, indexFieldRemap);
                        } else {
                            filter = null;
                        }

                        View.Append appender = view.getAppender(dataConsumer.getOutputType());
                        if (appender == null) {
                            return CompletableFuture.completedFuture(null);
                        }
                        try {
                            dataConsumer.getAll(appender::add, filter);
                        } catch (TooManyNodeException e) {
                            LOGGER.error("Too many node for request");
                            return CompletableFuture.failedFuture(e);
                        }
                        appender.complete();
                        Glob value = view.toGlob();
                        if (queryParameters.get(ParamType.outputType, "json").equals("csv")) {
                            return CompletableFuture.completedFuture(dumpInCsv(viewRequest, value, queryParameters.get(ParamType.leafOnly, false)));
                        }
                        return CompletableFuture.completedFuture(value);
                    }
                });

        HttpServerRegister.HttpStartup httpServerIntegerPair = httpServerRegister.startAndWaitForStartup(serverBootstrap);
        httpServer = httpServerIntegerPair.httpServer();
        port = httpServerIntegerPair.listenPort();
        System.out.println("PORT: " + port);
        LOGGER.info("http port : " + port);
    }

    public static Glob dumpInCsv(Glob request, Glob root, boolean leafOnly) throws IOException {
        Path content = Files.createTempFile("viewContent", ".csv");
        File file = content.toFile();
        FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
        Writer writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
        ExportBySize exportBySize = new ExportBySize();
        exportBySize.withSeparator(';');
        CsvExporter.toCsv(request, root, leafOnly, new ExportBySize.LineWriterToWriter(writer), exportBySize, true);
        writer.close();
        return GlobFile.TYPE.instantiate()
                .set(GlobFile.removeWhenDelivered, true)
                .set(GlobFile.mimeType, "text/csv")
                .set(GlobFile.file, file.getAbsolutePath());

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

        public static StringField localViewAddress;

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
