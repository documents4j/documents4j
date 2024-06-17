package com.documents4j.standalone;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.api.IFileConsumer;
import com.documents4j.job.RemoteConverter;
import joptsimple.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(StandaloneClient.class);

    private static final Map<DocumentType, String> FILE_NAME_EXTENSIONS;

    static {
        FILE_NAME_EXTENSIONS = new HashMap<>();
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
        FILE_NAME_EXTENSIONS.put(DocumentType.PPT, "ppt");
        FILE_NAME_EXTENSIONS.put(DocumentType.PPTX, "pptx");
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
                LOG.error("This application can only be used from the command line.");
                System.exit(-1);
            }
            IConverter converter = asConverter(args);
            try {
                sayHello(converter, console);
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
                        } else if (argument.trim().isEmpty()) {
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
                                .to(targetFile, new LoggingFileConsumer(sourceFile, LOG)).as(documentTypes[1])
                                .schedule();
                        console.printf("Scheduled conversion: %s -> %s%n", sourceFile, targetFile);
                        LOG.info("Converting {} to {}", sourceFile, targetFile);
                    } else {
                        LOG.error("Error when reading from console.");
                    }
                } while (argument != null);
                sayGoodbye(converter);
            } finally {
                converter.shutDown();
            }
            console.printf("The connection was successfully closed. Goodbye!%n");
        } catch (Exception e) {
            LoggerFactory.getLogger(StandaloneClient.class).error("The document conversion client terminated with an unexpected error", e);
            LOG.error("Error: {}. Use option -? to display a list of legal commands.", e.getMessage(), e);
            System.exit(-1);
        }
    }

    private static DocumentType[] configureConversion(Console console, Map<DocumentType, Set<DocumentType>> supportedConversions) {
        console.printf("The connected converter supports the following conversion formats:%n");
        Map<Integer, DocumentType[]> conversionsByIndex = new HashMap<>();
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

        OptionSet optionSet;
        try {
            optionSet = optionParser.parse(args);
        } catch (OptionException e) {
            LOG.error("The converter was started with unknown arguments: {}", e.options());
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
            LOG.error("No base URI parameter specified. (Use: <command> <base URI>)");
            System.exit(-1);
        }

        long requestTimeout = requestTimeoutSpec.value(optionSet);
        checkArgument(requestTimeout >= 0L, "The request timeout timeout must not be negative");

        System.out.println("Connecting to: " + baseUri);

        RemoteConverter.Builder builder = RemoteConverter.builder()
                .requestTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                .baseUri(baseUri);
        if (optionSet.has(sslSpec)) {
            try {
                builder.sslContext(SSLContext.getDefault());
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

    private static OptionSpec<Void> makeHelpSpec(OptionParser optionParser) {
        return optionParser
                .acceptsAll(Arrays.asList(
                        CommandDescription.ARGUMENT_LONG_HELP,
                        CommandDescription.ARGUMENT_SHORT_HELP),
                        CommandDescription.DESCRIPTION_CONTEXT_HELP
                )
                .forHelp();
    }

    private static void sayHello(IConverter converter, Console console) {
        console.printf("Welcome to the documents4j client!%n");
        boolean operational = converter.isOperational();
        if (operational) {
            LOG.info("Converter {} is operational", converter);
        } else {
            LOG.warn("Converter {} is not operational", converter);
        }
    }

    private static void sayGoodbye(IConverter converter) {
        System.out.println("Disconnecting converter client...");
        LOG.info("Converter {} is disconnecting", converter);
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
