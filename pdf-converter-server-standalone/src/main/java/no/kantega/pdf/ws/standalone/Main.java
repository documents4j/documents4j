package no.kantega.pdf.ws.standalone;

import joptsimple.*;
import no.kantega.pdf.builder.ConverterServerBuilder;
import no.kantega.pdf.job.LocalConverter;
import no.kantega.pdf.ws.application.IWebConverterConfiguration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Entry point for a command-line invoked standalone conversion server.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Starts a standalone conversion server. Detailed documentation can be retrieved by invoking
     * the application via the command line.
     *
     * @param args The parameters for configuring this server.
     */
    public static void main(String[] args) {
        try {
            ConverterServerBuilder builder = asBuilder(args);
            HttpServer httpServer = builder.build();
            try {
                sayHello(builder);
                System.out.println("PDF-conversion server is up and running. Hit enter to shut down...");
                if(System.in.read() == -1) {
                    LOGGER.warn("Console read terminated without receiving a user input");
                }
                sayGoodbye(builder);
            } catch (IOException e) {
                LOGGER.error("Error when reading from the console", e);
                throw new RuntimeException("A console error occurred", e);
            } finally {
                httpServer.stop();
            }
            System.out.println("Shut down successful. Goodbye!");
        } catch (Exception e) {
            System.err.println(String.format("Error: %s", e.getMessage()));
            System.err.println("Use option -? to display a list of legal commands.");
            System.exit(-1);
        }
    }

    private static ConverterServerBuilder asBuilder(String[] args) throws IOException {

        OptionParser optionParser = new OptionParser();

        OptionSpec<?> helpSpec = makeHelpSpec(optionParser);

        NonOptionArgumentSpec<URI> baseUriSpec = makeBaseUriSpec(optionParser);

        ArgumentAcceptingOptionSpec<File> baseFolderSpec = makeBaseFolderSpec(optionParser);
        ArgumentAcceptingOptionSpec<Integer> corePoolSizeSpec = makeCorePoolSizeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Integer> fallbackPoolSizeSpc = makeFallbackPoolSizeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> keepAliveTimeSpec = makeKeepAliveTimeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> processTimeoutSpec = makeProcessTimeoutSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> requestTimeoutSpec = makeRequestTimeoutSpec(optionParser);

        ArgumentAcceptingOptionSpec<File> logFileSpec = makeLogFileSpec(optionParser);

        OptionSet optionSet = optionParser.parse(args);

        if (optionSet.has(helpSpec)) {
            optionParser.printHelpOn(System.out);
            System.exit(0);
        }

        URI baseUri = baseUriSpec.value(optionSet);
        if (baseUri == null) {
            throw new NullPointerException("No base URI parameter specified. (Use: <command> <base URI>)");
        }

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
        configureLogging(logFile);

        return ConverterServerBuilder.builder()
                .baseUri(baseUri)
                .baseFolder(baseFolder)
                .workerPool(corePoolSize, corePoolSize + fallbackPoolSize, keepAliveTime, TimeUnit.MILLISECONDS)
                .processTimeout(processTimeout, TimeUnit.MILLISECONDS)
                .requestTimeout(requestTimeout, TimeUnit.MILLISECONDS);
    }

    private static void configureLogging(File logFile) {
        String logKey;
        if (logFile == null) {
            logKey = "System.err";
        } else {
            logKey = logFile.getAbsolutePath();
        }
        System.setProperty(SimpleLogger.LOG_FILE_KEY, logKey);
    }

    private static ArgumentAcceptingOptionSpec<File> makeBaseFolderSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_BASE_FOLDER,
                        CommandDescription.ARGUMENT_SHORT_BASE_FOLDER),
                        CommandDescription.DESCRIPTION_CONTEXT_BASE_FOLDER)
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
                        CommandDescription.DESCRIPTION_CONTEXT_CORE_POOL_SIZE)
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
                        CommandDescription.DESCRIPTION_CONTEXT_MAXIMUM_POOL_SIZE)
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
                        CommandDescription.DESCRIPTION_CONTEXT_KEEP_ALIVE_TIME)
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
                        CommandDescription.DESCRIPTION_CONTEXT_PROCESS_TIME_OUT)
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_PROCESS_TIME_OUT)
                .ofType(Long.class)
                .defaultsTo(LocalConverter.Builder.DEFAULT_PROCESS_TIME_OUT);
    }

    private static ArgumentAcceptingOptionSpec<Long> makeRequestTimeoutSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_REQUEST_TIME_OUT,
                        CommandDescription.ARGUMENT_SHORT_REQUEST_TIME_OUT),
                        CommandDescription.DESCRIPTION_CONTEXT_REQUEST_TIME_OUT)
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_REQUEST_TIME_OUT)
                .ofType(Long.class)
                .defaultsTo(IWebConverterConfiguration.DEFAULT_REQUEST_TIME_OUT);
    }

    private static ArgumentAcceptingOptionSpec<File> makeLogFileSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_LOG_TO_FILE,
                        CommandDescription.ARGUMENT_SHORT_LOG_TO_FILE),
                        CommandDescription.DESCRIPTION_CONTEXT_LOG_TO_FILE)
                .withRequiredArg()
                .describedAs(CommandDescription.DESCRIPTION_ARGUMENT_LOG_TO_FILE)
                .ofType(File.class);
        // defaults to null such that all log information is written to the console
    }

    private static NonOptionArgumentSpec<URI> makeBaseUriSpec(OptionParser optionParser) {
        return optionParser.nonOptions(CommandDescription.DESCRIPTION_BASE_URI).ofType(URI.class);
    }

    private static OptionSpec<Void> makeHelpSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_HELP,
                        CommandDescription.ARGUMENT_SHORT_HELP),
                        CommandDescription.DESCRIPTION_CONTEXT_HELP)
                .forHelp();
    }

    private static void sayHello(ConverterServerBuilder builder) {
        String serverStartupMessage = String.format("%tc: Started server on '%s'",
                System.currentTimeMillis(), builder.getBaseUri());
        LOGGER.info(serverStartupMessage);
        logServerInfo(builder);
        System.out.println(serverStartupMessage);
    }

    private static void logServerInfo(ConverterServerBuilder builder) {
        LOGGER.info(" --------- Server configuration --------- ");
        LOGGER.info("Listening at: {}", builder.getBaseUri());
        LOGGER.info("All files are written to: {}", builder.getBaseFolder() == null ? "<temporary folder>" : builder.getBaseFolder());
        LOGGER.info("Worker threads: {} (+{}) - timeout: {} ms", builder.getCorePoolSize(),
                builder.getMaximumPoolSize(), builder.getKeepAliveTime());
        LOGGER.info("Process timeout: {}", builder.getProcessTimeout());
        LOGGER.info("Request timeout: {}", builder.getRequestTimeout());
        LOGGER.info(" ---------------------------------------- ");
    }

    private static void sayGoodbye(ConverterServerBuilder builder) {
        String serverShutdownMessage = String.format("%tc: Shutting down server on '%s'",
                System.currentTimeMillis(), builder.getBaseUri());
        LOGGER.info(serverShutdownMessage);
        System.out.println(serverShutdownMessage);
    }
}
