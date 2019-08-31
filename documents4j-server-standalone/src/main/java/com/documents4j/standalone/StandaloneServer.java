package com.documents4j.standalone;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
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
            ConverterServerBuilder builder = asBuilder(args);
            HttpServer httpServer = builder.build();
            Logger logger = LoggerFactory.getLogger(StandaloneServer.class);
            try {
                sayHello(builder, logger);
                if (builder.isServiceMode()) {
                    System.out.println("The documents4j server is up and running in service mode and will not terminate until process interruption.");
                    try {
                        Thread.currentThread().join();
                    } catch (InterruptedException ignored) {
                        System.out.println("Received interruption signal which indicates server termination.");
                    }
                } else {
                    System.out.println("The documents4j server is up and running. Hit the enter key to shut it down...");
                    if (System.in.read() == -1) {
                        System.out.println("Console read terminated without receiving user input");
                    }
                }
                sayGoodbye(builder, logger);
            } finally {
                httpServer.shutdownNow();
            }
            System.out.println("Shut down successful. Goodbye!");
        } catch (Exception e) {
            LoggerFactory.getLogger(StandaloneServer.class).error("The documents4j server terminated with an unexpected error", e);
            System.err.println(String.format("Error: %s", e.getMessage()));
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
        ArgumentAcceptingOptionSpec<File> logFileSpec = makeLogFileSpec(optionParser);
        ArgumentAcceptingOptionSpec<Level> logLevelSpec = makeLogLevelSpec(optionParser);

        OptionSet optionSet;
        try {
            optionSet = optionParser.parse(args);
        } catch (OptionException e) {
            System.out.println("The converter was started with unknown arguments: " + e.options());
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
            System.out.println("No base URI parameter specified. (Use: <command> <base URI>)");
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

        File logFile = logFileSpec.value(optionSet);
        Level level = logLevelSpec.value(optionSet);
        configureLogging(logFile, level);

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
                System.out.println("Could not access default SSL context: " + e.getMessage());
                System.exit(-1);
            }
        }
        if (optionSet.hasArgument(authSpec)) {
            builder.userPass(authSpec.value(optionSet));
        }
        return builder.serviceMode(serviceMode);
    }

    private static void configureLogging(File logFile, Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        OutputStreamAppender<ILoggingEvent> appender;
        if (logFile == null) {
            appender = configureConsoleLogging(loggerContext);
        } else {
            appender = configureFileLogging(logFile, loggerContext);
        }
        System.out.println("Logging: The log level is set to " + level);
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern(LogDescription.LOG_PATTERN);
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();
        appender.setEncoder(patternLayoutEncoder);
        appender.start();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        loggerContext.stop();
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(appender);
        rootLogger.setLevel(level);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
        levelChangePropagator.setResetJUL(true);
        levelChangePropagator.setContext(loggerContext);
        levelChangePropagator.start();
        loggerContext.addListener(levelChangePropagator);
        loggerContext.start();
    }

    private static OutputStreamAppender<ILoggingEvent> configureConsoleLogging(LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setName("com.documents4j.logger.server.console");
        consoleAppender.setContext(loggerContext);
        System.out.println("Logging: The log is printed to the console");
        return consoleAppender;
    }

    private static OutputStreamAppender<ILoggingEvent> configureFileLogging(File logFile, LoggerContext loggerContext) {
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setFile(logFile.getAbsolutePath());
        rollingFileAppender.setName("com.documents4j.logger.server.file");
        rollingFileAppender.setContext(loggerContext);
        FixedWindowRollingPolicy fixedWindowRollingPolicy = new FixedWindowRollingPolicy();
        fixedWindowRollingPolicy.setFileNamePattern(logFile.getAbsolutePath() + ".%i.gz");
        fixedWindowRollingPolicy.setMaxIndex(LogDescription.MAXIMUM_LOG_HISTORY_INDEX);
        fixedWindowRollingPolicy.setContext(loggerContext);
        fixedWindowRollingPolicy.setParent(rollingFileAppender);
        SizeBasedTriggeringPolicy<ILoggingEvent> sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        sizeBasedTriggeringPolicy.setMaxFileSize(FileSize.valueOf(LogDescription.MAXIMUM_LOG_FILE_SIZE));
        sizeBasedTriggeringPolicy.setContext(loggerContext);
        rollingFileAppender.setRollingPolicy(fixedWindowRollingPolicy);
        rollingFileAppender.setTriggeringPolicy(sizeBasedTriggeringPolicy);
        sizeBasedTriggeringPolicy.start();
        fixedWindowRollingPolicy.start();
        System.out.println("Logging: The log is written to " + logFile);
        return rollingFileAppender;
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

    private static ArgumentAcceptingOptionSpec<File> makeLogFileSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_LOG_TO_FILE,
                        CommandDescription.ARGUMENT_SHORT_LOG_TO_FILE),
                        CommandDescription.DESCRIPTION_CONTEXT_LOG_TO_FILE
                )
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_LOG_TO_FILE)
                .ofType(File.class);
        // defaults to null such that all log information is written to the console
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

    private static void sayHello(ConverterServerBuilder builder, Logger logger) {
        System.out.println("Welcome to the documents4j server!");
        String serverStartupMessage = String.format("%tc: Started server on '%s'", System.currentTimeMillis(), builder.getBaseUri());
        logger.info(serverStartupMessage);
        logServerInfo(builder, logger);
        System.out.println(serverStartupMessage);
    }

    private static void logServerInfo(ConverterServerBuilder builder, Logger logger) {
        logger.info("documents4j server is listening at {}", builder.getBaseUri());
        logger.info("documents4j server is writing temporary files to: {}",
                builder.getBaseFolder() == null ? "<temporary folder>" : builder.getBaseFolder());
        logger.info("documents4j server worker threads: {} (+{}) - timeout: {} ms",
                builder.getCorePoolSize(), builder.getMaximumPoolSize(), builder.getKeepAliveTime());
        logger.info("documents4j server process timeout: {}", builder.getProcessTimeout());
        logger.info("documents4j server request timeout: {}", builder.getRequestTimeout());
    }

    private static void sayGoodbye(ConverterServerBuilder builder, Logger logger) {
        String serverShutdownMessage = String.format("%tc: Shutting down server on '%s'", System.currentTimeMillis(), builder.getBaseUri());
        logger.info(serverShutdownMessage);
        System.out.println(serverShutdownMessage);
    }
}
