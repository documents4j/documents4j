package com.documents4j.standalone;

import ch.qos.logback.classic.Level;
import com.documents4j.builder.ConverterServerBuilder;
import com.documents4j.conversion.IExternalConverter;
import com.documents4j.job.LocalConverter;
import com.documents4j.ws.application.IWebConverterConfiguration;
import joptsimple.*;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Entry point for a command-line invoked standalone conversion server.
 */
public class StandaloneServer {
    private static final Logger LOG = LoggerFactory.getLogger(StandaloneServer.class);

    private StandaloneServer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts a standalone conversion server. Detailed documentation can be retrieved by invoking
     * the application via the command line with the {@code -?} option.
     *
     * @param args The parameters for configuring this server.
     */
    public static void main(String[] args) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            ConverterServerBuilder builder = asBuilder(args);
            HttpServer httpServer = builder.build();
            try {
                sayHello(builder);
                if (builder.isServiceMode()) {
                    LOG.info("The documents4j server is up and running in service mode and will not terminate until process interruption.");
                    try {
                        Thread.currentThread().join();
                    } catch (InterruptedException ignored) {
                        LOG.info("Received interruption signal which indicates server termination.");
                    }
                } else {
                    LOG.info("The documents4j server is up and running. Hit the enter key to shut it down...");
                    if (System.in.read() == -1) {
                        LOG.info("Console read terminated without receiving user input");
                    }
                }
                sayGoodbye(builder);
            } finally {
                httpServer.shutdownNow();
            }
            LOG.info("Shut down successful. Goodbye!");
        } catch (Exception e) {
            LoggerFactory.getLogger(StandaloneServer.class).error("The documents4j server terminated with an unexpected error", e);
            System.err.printf("Error: %s%n", e.getMessage());
            System.err.println("Use option -? to display a list of legal commands.");
            System.exit(-1);
        }
    }

    private static ConverterServerBuilder asBuilder(String[] args) throws IOException {

        OptionParser optionParser = new OptionParser();

        OptionSpec<?> helpSpec = makeHelpSpec(optionParser);

        NonOptionArgumentSpec<URI> baseUriSpec = makeBaseUriSpec(optionParser);

        OptionSpec<?> serviceModeSpec = makeServiceModeSpec(optionParser);

        ArgumentAcceptingOptionSpec<File> baseFolderSpec = makeBaseFolderSpec(optionParser);
        ArgumentAcceptingOptionSpec<Integer> corePoolSizeSpec = makeCorePoolSizeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Integer> fallbackPoolSizeSpc = makeFallbackPoolSizeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> keepAliveTimeSpec = makeKeepAliveTimeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> processTimeoutSpec = makeProcessTimeoutSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> requestTimeoutSpec = makeRequestTimeoutSpec(optionParser);

        ArgumentAcceptingOptionSpec<Class<? extends IExternalConverter>> converterEnabledSpec = makeConverterEnabledSpec(optionParser);
        ArgumentAcceptingOptionSpec<Class<? extends IExternalConverter>> converterDisabledSpec = makeConverterDisabledSpec(optionParser);

        OptionSpec<?> sslSpec = makeSslSpec(optionParser);

        ArgumentAcceptingOptionSpec<String> authSpec = makeAuthSpec(optionParser);
        ArgumentAcceptingOptionSpec<Level> logLevelSpec = makeLogLevelSpec(optionParser);

        OptionSet optionSet;
        try {
            optionSet = optionParser.parse(args);
        } catch (OptionException e) {
            LOG.info("The converter was started with unknown arguments: {}", e.options());
            optionParser.printHelpOn(System.out);
            System.exit(-1);
            throw e; // To satisfy the Java compiler.
        }

        if (optionSet.has(helpSpec)) {
            optionParser.printHelpOn(System.out);
            System.exit(0);
        }

        URI baseUri = baseUriSpec.value(optionSet);
        if (baseUri == null) {
            LOG.info("No base URI parameter specified. (Use: <command> <base URI>)");
            System.exit(-1);
        }

        boolean serviceMode = optionSet.has(serviceModeSpec);

        File baseFolder = baseFolderSpec.value(optionSet);
        checkArgument(baseFolder == null || baseFolder.exists(), "The specified base folder cannot be located on the file system");
        int corePoolSize = corePoolSizeSpec.value(optionSet);
        checkArgument(corePoolSize >= 0, "The number of core worker threads must not be negative");
        int fallbackPoolSize = fallbackPoolSizeSpc.value(optionSet);
        checkArgument(fallbackPoolSize >= 0, "The number of fallback worker threads must not be negative");
        checkArgument(corePoolSize + fallbackPoolSize > 0, "The number of worker threads must be positive");
        long keepAliveTime = keepAliveTimeSpec.value(optionSet);
        checkArgument(keepAliveTime >= 0L, "The worker thread keep alive time must not be negative");
        long processTimeout = processTimeoutSpec.value(optionSet);
        checkArgument(processTimeout >= 0L, "The process timeout timeout must not be negative");
        long requestTimeout = requestTimeoutSpec.value(optionSet);
        checkArgument(requestTimeout >= 0L, "The request timeout timeout must not be negative");

        Level level = logLevelSpec.value(optionSet);
        LOG.info("Logging: The log level is set to " + level);

        ConverterServerBuilder builder = ConverterServerBuilder.builder()
                .baseUri(baseUri)
                .baseFolder(baseFolder)
                .workerPool(corePoolSize, corePoolSize + fallbackPoolSize, keepAliveTime, TimeUnit.MILLISECONDS)
                .processTimeout(processTimeout, TimeUnit.MILLISECONDS)
                .requestTimeout(requestTimeout, TimeUnit.MILLISECONDS);
        for (Class<? extends IExternalConverter> externalConverter : converterDisabledSpec.values(optionSet)) {
            builder = builder.disable(externalConverter);
        }
        for (Class<? extends IExternalConverter> externalConverter : converterEnabledSpec.values(optionSet)) {
            builder = builder.enable(externalConverter);
        }
        if (optionSet.has(sslSpec)) {
            try {
                builder = builder.sslContext(SSLContext.getDefault());
            } catch (NoSuchAlgorithmException e) {
                LOG.info("Could not access default SSL context: " + e.getMessage());
                System.exit(-1);
            }
        }
        if (optionSet.hasArgument(authSpec)) {
            builder.userPass(authSpec.value(optionSet));
        }
        return builder.serviceMode(serviceMode);
    }

    private static ArgumentAcceptingOptionSpec<File> makeBaseFolderSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_BASE_FOLDER,
                        CommandDescription.ARGUMENT_SHORT_BASE_FOLDER),
                        CommandDescription.DESCRIPTION_CONTEXT_BASE_FOLDER
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_BASE_FOLDER)
                .ofType(File.class);
        // Defaults to null such that the builder will create a random temporary folder.
    }

    private static ArgumentAcceptingOptionSpec<Integer> makeCorePoolSizeSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_CORE_POOL_SIZE,
                        CommandDescription.ARGUMENT_SHORT_CORE_POOL_SIZE),
                        CommandDescription.DESCRIPTION_CONTEXT_CORE_POOL_SIZE
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_CORE_POOL_SIZE)
                .ofType(Integer.class)
                .defaultsTo(LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE);
    }

    private static ArgumentAcceptingOptionSpec<Integer> makeFallbackPoolSizeSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_MAXIMUM_POOL_SIZE,
                        CommandDescription.ARGUMENT_SHORT_MAXIMUM_POOL_SIZE),
                        CommandDescription.DESCRIPTION_CONTEXT_MAXIMUM_POOL_SIZE
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_MAXIMUM_POOL_SIZE)
                .ofType(Integer.class)
                .defaultsTo(LocalConverter.Builder.DEFAULT_MAXIMUM_POOL_SIZE - LocalConverter.Builder.DEFAULT_CORE_POOL_SIZE);
    }

    private static ArgumentAcceptingOptionSpec<Long> makeKeepAliveTimeSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_KEEP_ALIVE_TIME,
                        CommandDescription.ARGUMENT_SHORT_KEEP_ALIVE_TIME),
                        CommandDescription.DESCRIPTION_CONTEXT_KEEP_ALIVE_TIME
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_THREAD_POOL_FALLBACK_LIFE_TIME)
                .ofType(Long.class)
                .defaultsTo(LocalConverter.Builder.DEFAULT_KEEP_ALIVE_TIME);
    }

    private static ArgumentAcceptingOptionSpec<Long> makeProcessTimeoutSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_PROCESS_TIME_OUT,
                        CommandDescription.ARGUMENT_SHORT_PROCESS_TIME_OUT),
                        CommandDescription.DESCRIPTION_CONTEXT_PROCESS_TIME_OUT
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_PROCESS_TIME_OUT)
                .ofType(Long.class)
                .defaultsTo(LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT);
    }

    private static ArgumentAcceptingOptionSpec<Long> makeRequestTimeoutSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_REQUEST_TIMEOUT,
                        CommandDescription.ARGUMENT_SHORT_REQUEST_TIMEOUT),
                        CommandDescription.DESCRIPTION_CONTEXT_REQUEST_TIMEOUT
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_REQUEST_TIMEOUT)
                .ofType(Long.class)
                .defaultsTo(IWebConverterConfiguration.DEFAULT_REQUEST_TIMEOUT);
    }

    private static OptionSpec<?> makeSslSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_SSL,
                        CommandDescription.ARGUMENT_SHORT_SSL),
                        CommandDescription.DESCRIPTION_CONTEXT_SSL
                );
    }

    private static ArgumentAcceptingOptionSpec<String> makeAuthSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_AUTH,
                        CommandDescription.ARGUMENT_SHORT_AUTH),
                        CommandDescription.DESCRIPTION_CONTEXT_AUTH
                ).withRequiredArg()
                .ofType(String.class);
    }

    private static ArgumentAcceptingOptionSpec<Level> makeLogLevelSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_LOG_LEVEL,
                        CommandDescription.ARGUMENT_SHORT_LOG_LEVEL),
                        CommandDescription.DESCRIPTION_CONTEXT_LOG_LEVEL
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_LOG_LEVEL)
                .withValuesConvertedBy(new LogLevelValueConverter())
                .defaultsTo(Level.WARN);
    }

    private static ArgumentAcceptingOptionSpec<Class<? extends IExternalConverter>> makeConverterDisabledSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_DISABLED_CONVERTER,
                        CommandDescription.ARGUMENT_SHORT_DISABLED_CONVERTER),
                        CommandDescription.DESCRIPTION_CONTEXT_DISABLED_CONVERTER
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_DISABLED_CONVERTER)
                .withValuesConvertedBy(new ExternalConverterValueConverter());
    }

    private static ArgumentAcceptingOptionSpec<Class<? extends IExternalConverter>> makeConverterEnabledSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_ENABLED_CONVERTER,
                        CommandDescription.ARGUMENT_SHORT_ENABLED_CONVERTER),
                        CommandDescription.DESCRIPTION_CONTEXT_ENABLED_CONVERTER
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_ENABLED_CONVERTER)
                .withValuesConvertedBy(new ExternalConverterValueConverter());
    }

    private static NonOptionArgumentSpec<URI> makeBaseUriSpec(OptionParser optionParser) {
        return optionParser.nonOptions(CommandDescription.DESCRIPTION_BASE_URI).ofType(URI.class);
    }

    private static OptionSpec<Void> makeHelpSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_HELP,
                        CommandDescription.ARGUMENT_SHORT_HELP),
                        CommandDescription.DESCRIPTION_CONTEXT_HELP
                )
                .forHelp();
    }

    private static OptionSpec<Void> makeServiceModeSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_SERVICE_MODE,
                        CommandDescription.ARGUMENT_SHORT_SERVICE_MODE),
                        CommandDescription.DESCRIPTION_CONTEXT_SERVICE_MODE
                );
    }

    private static void sayHello(ConverterServerBuilder builder) {
        LOG.info("Welcome to the documents4j server!");
        String serverStartupMessage = String.format("%tc: Started server on '%s'", System.currentTimeMillis(), builder.getBaseUri());
        LOG.info(serverStartupMessage);
        logServerInfo(builder);
        LOG.info(serverStartupMessage);
    }

    private static void logServerInfo(ConverterServerBuilder builder) {
        LOG.info("documents4j server is listening at {}", builder.getBaseUri());
        LOG.info("documents4j server is writing temporary files to: {}",
                builder.getBaseFolder() == null ? "<temporary folder>" : builder.getBaseFolder());
        LOG.info("documents4j server worker threads: {} (+{}) - timeout: {} ms",
                builder.getCorePoolSize(), builder.getMaximumPoolSize(), builder.getKeepAliveTime());
        LOG.info("documents4j server process timeout: {}", builder.getProcessTimeout());
        LOG.info("documents4j server request timeout: {}", builder.getRequestTimeout());
    }

    private static void sayGoodbye(ConverterServerBuilder builder) {
        String serverShutdownMessage = String.format("%tc: Shutting down server on '%s'", System.currentTimeMillis(), builder.getBaseUri());
        LOG.info(serverShutdownMessage);
        LOG.info(serverShutdownMessage);
    }
}
