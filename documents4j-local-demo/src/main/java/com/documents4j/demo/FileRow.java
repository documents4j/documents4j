package com.documents4j.demo;

import com.documents4j.api.DocumentType;

import java.io.*;
import java.util.*;

public class FileRow implements Serializable {

    public static final String SOURCE_FILE_NAME = "source.raw";
    public static final String TARGET_FILE_NAME = "target.raw";
    public static final String PROPERTIES_FILE_NAME = "conversion.properties";

    public static final String INPUT_NAME_PROPERTY_KEY = "name";
    public static final String SOURCE_FORMAT = "source";
    public static final String TARGET_FORMAT = "target";
    public static final String CONVERSION_DURATION_PROPERTY_KEY = "length";

    public static final String PROPERTY_COMMENT = "documents4j demo record";
    private static final Map<String, String> FILE_EXTENSIONS;

    static {
        FILE_EXTENSIONS = new HashMap<String, String>();
        FILE_EXTENSIONS.put(DocumentType.DOC.toString(), "doc");
        FILE_EXTENSIONS.put(DocumentType.DOCX.toString(), "docx");
        FILE_EXTENSIONS.put(DocumentType.XLS.toString(), "xls");
        FILE_EXTENSIONS.put(DocumentType.XLSX.toString(), "xlsx");
        FILE_EXTENSIONS.put(DocumentType.ODS.toString(), "ods");
        FILE_EXTENSIONS.put(DocumentType.PDF.toString(), "pdf");
        FILE_EXTENSIONS.put(DocumentType.PDFA.toString(), "pdf");
        FILE_EXTENSIONS.put(DocumentType.MHTML.toString(), "mhtml");
        FILE_EXTENSIONS.put(DocumentType.RTF.toString(), "rtf");
        FILE_EXTENSIONS.put(DocumentType.XML.toString(), "xml");
        FILE_EXTENSIONS.put(DocumentType.TEXT.toString(), "txt");
        FILE_EXTENSIONS.put(DocumentType.CSV.toString(), "csv");
    }

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

    public String getSourceFormat() {
        return properties.getProperty(SOURCE_FORMAT, "(unknown)");
    }

    public String getTargetFormat() {
        return properties.getProperty(TARGET_FORMAT, "(unknown)");
    }

    public String getSourceName() {
        return properties.getProperty(INPUT_NAME_PROPERTY_KEY, "input");
    }

    public String getOutputName() {
        return properties.getProperty(INPUT_NAME_PROPERTY_KEY, "output") + "." + findFileExtension();
    }

    private String findFileExtension() {
        String extension = FILE_EXTENSIONS.get(getTargetFormat());
        return extension == null ? "converted" : extension;
    }
}
