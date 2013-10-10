package no.kantega.pdf.ws.standalone;

import joptsimple.*;
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

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            UserConfiguration userConfiguration = readConfiguration(args);
            HttpServer httpServer = userConfiguration.makeServer();
            try {
                sayHello(userConfiguration);
                userConfiguration.pretty(System.out);
                System.out.println("PDF-conversion server is up and running. Hit enter to shut down...");
                System.in.read();
                sayGoodbye(userConfiguration);
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

    private static UserConfiguration readConfiguration(String[] args) throws IOException {

        OptionParser optionParser = new OptionParser();

        OptionSpec<?> helpSpec = makeHelpSpec(optionParser);

        NonOptionArgumentSpec<URI> serverBaseUriSpec = makeServerBaseUriSpec(optionParser);

        ArgumentAcceptingOptionSpec<File> baseFolderSpec = makeBaseFolderSpec(optionParser);
        ArgumentAcceptingOptionSpec<Integer> corePoolSizeSpec = makeCorePoolSizeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Integer> fallbackPoolSizeSpc = makeFallbackPoolSizeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> keepAliveTimeSpec = makeKeepAliveTimeSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> processTimeOutSpec = makeProcessTimeOutSpec(optionParser);
        ArgumentAcceptingOptionSpec<Long> requestTimeOutSpec = makeRequestTimeOutSpec(optionParser);

        ArgumentAcceptingOptionSpec<File> logFileSpec = makeLogFileSpec(optionParser);

        OptionSet optionSet = optionParser.parse(args);

        if (optionSet.has(helpSpec)) {
            optionParser.printHelpOn(System.out);
            System.exit(0);
        }

        URI serverBaseUri = serverBaseUriSpec.value(optionSet);
        if (serverBaseUri == null) {
            throw new NullPointerException("No base URI parameter specified. (Use: <command> <base URI>)");
        }

        File baseFolder = baseFolderSpec.value(optionSet);
        int corePoolSize = corePoolSizeSpec.value(optionSet);
        int fallbackPoolSize = fallbackPoolSizeSpc.value(optionSet);
        long keepAliveTime = keepAliveTimeSpec.value(optionSet);
        long processTimeOut = processTimeOutSpec.value(optionSet);
        long requestTimeOut = requestTimeOutSpec.value(optionSet);

        File logFile = logFileSpec.value(optionSet);
        configureLogging(logFile);

        return new UserConfiguration(serverBaseUri, baseFolder, corePoolSize, fallbackPoolSize,
                keepAliveTime, processTimeOut, requestTimeOut);
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
        // defaults to null such that builder will create a random temporary folder
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

    private static ArgumentAcceptingOptionSpec<Long> makeProcessTimeOutSpec(OptionParser optionParser) {
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

    private static ArgumentAcceptingOptionSpec<Long> makeRequestTimeOutSpec(OptionParser optionParser) {
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

    private static NonOptionArgumentSpec<URI> makeServerBaseUriSpec(OptionParser optionParser) {
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

    private static void sayHello(UserConfiguration userConfiguration) {
        String serverStartupMessage = String.format("%tc: Started server on '%s'", System.currentTimeMillis(),
                userConfiguration.getServerBaseUri());
        LOGGER.info(serverStartupMessage);
        System.out.println(serverStartupMessage);
    }

    private static void sayGoodbye(UserConfiguration userConfiguration) {
        String serverShutdownMessage = String.format("%tc: Shutting down server on '%s'", System.currentTimeMillis(),
                userConfiguration.getServerBaseUri());
        LOGGER.info(serverShutdownMessage);
        System.out.println(serverShutdownMessage);
    }

}
