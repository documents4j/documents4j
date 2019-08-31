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
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.api.IFileConsumer;
import com.documents4j.job.RemoteConverter;
import joptsimple.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.SSLContext;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A standalone client for communicating with a conversion server via command line.
 */
public class StandaloneClient {

    private static final Map<DocumentType, String> FILE_NAME_EXTENSIONS;

    static {
        FILE_NAME_EXTENSIONS = new HashMap<DocumentType, String>();
        FILE_NAME_EXTENSIONS.put(DocumentType.DOC, "doc");
        FILE_NAME_EXTENSIONS.put(DocumentType.DOCX, "docx");
        FILE_NAME_EXTENSIONS.put(DocumentType.XLS, "xls");
        FILE_NAME_EXTENSIONS.put(DocumentType.XLSX, "xlsx");
        FILE_NAME_EXTENSIONS.put(DocumentType.ODS, "ods");
        FILE_NAME_EXTENSIONS.put(DocumentType.PDF, "pdf");
        FILE_NAME_EXTENSIONS.put(DocumentType.PDFA, "pdf");
        FILE_NAME_EXTENSIONS.put(DocumentType.MHTML, "mhtml");
        FILE_NAME_EXTENSIONS.put(DocumentType.RTF, "rtf");
        FILE_NAME_EXTENSIONS.put(DocumentType.XML, "xml");
        FILE_NAME_EXTENSIONS.put(DocumentType.TEXT, "txt");
        FILE_NAME_EXTENSIONS.put(DocumentType.CSV, "csv");
    }

    private StandaloneClient() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts a standalone conversion client. Detailed documentation can be retrieved by invoking
     * the application via the command line with the {@code -?} option.
     *
     * @param args The parameters for configuring this server.
     */
    public static void main(String[] args) {
        try {
            Console console = System.console();
            if (console == null) {
                System.out.println("This application can only be used from the command line.");
                System.exit(-1);
            }
            IConverter converter = asConverter(args);
            try {
                Logger logger = LoggerFactory.getLogger(StandaloneClient.class);
                sayHello(converter, logger, console);
                DocumentType[] documentTypes = configureConversion(console, converter.getSupportedConversions());
                console.printf("Enter '<source> [-> <target>]' for converting a file. Enter '\\q' for exiting this application.%n");
                String argument;
                do {
                    console.printf("> ");
                    argument = console.readLine();
                    if (argument != null) {
                        if (argument.equals("\\q")) {
                            break;
                        } else if (argument.equals("\\f")) {
                            documentTypes = configureConversion(console, converter.getSupportedConversions());
                        } else if (argument.trim().equals("")) {
                            continue;
                        }
                        int targetIndex = argument.indexOf("->");
                        String source = targetIndex == -1 ? argument : argument.substring(0, targetIndex);
                        File sourceFile = normalize(source);
                        if (!sourceFile.isFile()) {
                            console.printf("Input file does not exist: %s%n", sourceFile);
                            continue;
                        }
                        String target = targetIndex == -1 ? source + "." + extensionFor(documentTypes[1]) : argument.substring(targetIndex + 1);
                        File targetFile = normalize(target);
                        converter.convert(sourceFile).as(documentTypes[0])
                                .to(targetFile, new LoggingFileConsumer(sourceFile, logger)).as(documentTypes[1])
                                .schedule();
                        console.printf("Scheduled conversion: %s -> %s%n", sourceFile, targetFile);
                        logger.info("Converting {} to {}", sourceFile, targetFile);
                    } else {
                        logger.error("Error when reading from console.");
                    }
                } while (argument != null);
                sayGoodbye(converter, logger);
            } finally {
                converter.shutDown();
            }
            console.printf("The connection was successfully closed. Goodbye!%n");
        } catch (Exception e) {
            LoggerFactory.getLogger(StandaloneClient.class).error("The document conversion client terminated with an unexpected error", e);
            System.err.println(String.format("Error: %s", e.getMessage()));
            System.err.println("Use option -? to display a list of legal commands.");
            System.exit(-1);
        }
    }

    private static DocumentType[] configureConversion(Console console, Map<DocumentType, Set<DocumentType>> supportedConversions) {
        console.printf("The connected converter supports the following conversion formats:%n");
        Map<Integer, DocumentType[]> conversionsByIndex = new HashMap<Integer, DocumentType[]>();
        int index = 0;
        for (Map.Entry<DocumentType, Set<DocumentType>> entry : supportedConversions.entrySet()) {
            for (DocumentType targetType : entry.getValue()) {
                conversionsByIndex.put(index, new DocumentType[]{entry.getKey(), targetType});
                console.printf("  | [%d]: '%s' -> '%s'%n", index++, entry.getKey(), targetType);
            }
        }
        do {
            console.printf("Enter the number of the conversion you want to perform: ");
            try {
                int choice = Integer.parseInt(console.readLine());
                DocumentType[] conversion = conversionsByIndex.get(choice);
                if (conversion != null) {
                    console.printf("Converting '%s' to '%s'. You can change this setup by entering '\\f'.%n", conversion[0], conversion[1]);
                    return conversion;
                }
                console.printf("The number you provided is not among the legal choices%n");
            } catch (RuntimeException e) {
                console.printf("You did not provide a number%n");
            }
        } while (true);
    }

    private static String extensionFor(DocumentType documentType) {
        String fileNameExtension = FILE_NAME_EXTENSIONS.get(documentType);
        return fileNameExtension == null ? "converted" : fileNameExtension;
    }

    private static File normalize(String path) {
        File absolute = new File(path);
        if (absolute.isAbsolute()) {
            return absolute;
        } else {
            return new File(System.getProperty("user.dir"), path);
        }
    }

    private static IConverter asConverter(String[] args) throws IOException {

        OptionParser optionParser = new OptionParser();

        OptionSpec<?> helpSpec = makeHelpSpec(optionParser);

        NonOptionArgumentSpec<URI> baseUriSpec = makeBaseUriSpec(optionParser);

        ArgumentAcceptingOptionSpec<Long> requestTimeoutSpec = makeRequestTimeoutSpec(optionParser);

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
            throw e; // System.exit does not guarantee a JVM exit.
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

        long requestTimeout = requestTimeoutSpec.value(optionSet);
        checkArgument(requestTimeout >= 0L, "The request timeout timeout must not be negative");

        File logFile = logFileSpec.value(optionSet);
        Level level = logLevelSpec.value(optionSet);
        configureLogging(logFile, level);

        System.out.println("Connecting to: " + baseUri);

        RemoteConverter.Builder builder = RemoteConverter.builder()
                .requestTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                .baseUri(baseUri);
        if (optionSet.has(sslSpec)) {
            try {
                builder = builder.sslContext(SSLContext.getDefault());
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Could not access default SSL context: " + e.getMessage());
                System.exit(-1);
            }
        }
        final String userPass = authSpec.value(optionSet);
        if (userPass != null && userPass.contains(":")) {
            final String[] userPassArray = userPass.split(":", 2);
            builder.basicAuthenticationCredentials(userPassArray[0], userPassArray[1]);
        }
        return builder.build();
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
        consoleAppender.setName("com.documents4j.logger.client.console");
        consoleAppender.setContext(loggerContext);
        System.out.println("Logging: The log is printed to the console");
        return consoleAppender;
    }

    private static OutputStreamAppender<ILoggingEvent> configureFileLogging(File logFile, LoggerContext loggerContext) {
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setFile(logFile.getAbsolutePath());
        rollingFileAppender.setName("com.documents4j.logger.client.file");
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

    private static NonOptionArgumentSpec<URI> makeBaseUriSpec(OptionParser optionParser) {
        return optionParser.nonOptions(CommandDescription.DESCRIPTION_BASE_URI).ofType(URI.class);
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
                .defaultsTo(RemoteConverter.Builder.DEFAULT_REQUEST_TIMEOUT);
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

    private static OptionSpec<Void> makeHelpSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_HELP,
                        CommandDescription.ARGUMENT_SHORT_HELP),
                        CommandDescription.DESCRIPTION_CONTEXT_HELP
                )
                .forHelp();
    }

    private static void sayHello(IConverter converter, Logger logger, Console console) {
        console.printf("Welcome to the documents4j client!%n");
        boolean operational = converter.isOperational();
        if (operational) {
            logger.info("Converter {} is operational", converter);
        } else {
            logger.warn("Converter {} is not operational", converter);
        }
    }

    private static void sayGoodbye(IConverter converter, Logger logger) {
        System.out.println("Disconnecting converter client...");
        logger.info("Converter {} is disconnecting", converter);
    }

    private static class LoggingFileConsumer implements IFileConsumer {

        private final File sourceFile;

        private final Logger logger;

        private LoggingFileConsumer(File sourceFile, Logger logger) {
            this.sourceFile = sourceFile;
            this.logger = logger;
        }

        @Override
        public void onComplete(File file) {
            logger.info("Successfully converted {} to {}", sourceFile, file);
        }

        @Override
        public void onCancel(File file) {
            logger.warn("Conversion from {} to {} was cancelled", sourceFile, file);
        }

        @Override
        public void onException(File file, Exception e) {
            logger.error("Could not convert {} to {}", sourceFile, file, e);
        }
    }
}
