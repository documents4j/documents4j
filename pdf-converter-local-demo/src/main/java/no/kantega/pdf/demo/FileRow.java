package no.kantega.pdf.demo;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FileRow implements Serializable {

    public static final String SOURCE_FILE_NAME = "source.word";
    public static final String TARGET_FILE_NAME = "target.pdf";
    public static final String PROPERTIES_FILE_NAME = "conversion.properties";

    public static final String INPUT_NAME_PROPERTY_KEY = "name";
    public static final String CONVERSION_DURATION_PROPERTY_KEY = "length";

    public static final String PROPERTY_COMMENT = "Kantega PDF converter demo record";

    private final int row;
    private final File source;
    private final File target;
    private final Properties properties;

    public FileRow(int row, File source, File target, Properties properties) {
        this.row = row;
        this.source = source;
        this.target = target;
        this.properties = properties;
    }

    public File getSource() {
        return source;
    }

    public File getTarget() {
        return target;
    }

    public int getRow() {
        return row;
    }

    public String getDuration() {
        long milliseconds = Long.valueOf(properties.getProperty(CONVERSION_DURATION_PROPERTY_KEY, "-1"));
        if (milliseconds < 1000L) {
            return String.format("%d ms", milliseconds);
        } else {
            // Integral division is wanted in order to truncate the resulting value.
            double seconds = (milliseconds / 100L) / 10d;
            return String.format("~%.1f s", seconds);
        }
    }

    public String getSourceName() {
        return properties.getProperty(INPUT_NAME_PROPERTY_KEY, "input");
    }

    public String getOutputName() {
        return properties.getProperty(INPUT_NAME_PROPERTY_KEY, "output") + ".pdf";
    }

    public static List<FileRow> findAll() {

        File[] folders = DemoApplication.get().getUploadFolder().listFiles();
        if (folders == null) {
            throw new IllegalArgumentException("Argument must be a directory");
        }
        Arrays.sort(folders);

        List<FileRow> result = new ArrayList<FileRow>();

        int rowcount = 0;
        for (File folder : folders) {

            File source = new File(folder, SOURCE_FILE_NAME);
            File target = new File(folder, TARGET_FILE_NAME);
            Properties properties = loadProperties(folder);

            if (!source.exists() || !target.exists() || properties == null) {
                continue;
            }

            result.add(new FileRow(++rowcount, source, target, properties));
        }

        return result;
    }

    private static Properties loadProperties(File folder) {
        Properties properties = new Properties();
        File file = new File(folder, PROPERTIES_FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            try {
                properties.load(inputStream);
                return properties;
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            return null;
        }
    }
}