package com.documents4j.demo;

import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.simple.SimpleLogger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoApplication extends WebApplication {

    private IConverter converter;
    private File uploadFolder, baseFolder;

    private AtomicInteger nameSequence;

    public static DemoApplication get() {
        return (DemoApplication) WebApplication.get();
    }

    @Override
    public Class<? extends WebPage> getHomePage() {
        return DemoPage.class;
    }

    @Override
    public void init() {
        super.init();

        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        try {
            uploadFolder = Files.createTempDirectory("upload").toFile();
            baseFolder = Files.createTempDirectory("base").toFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        nameSequence = new AtomicInteger(1);

        converter = loadConverter();
    }

    protected IConverter loadConverter() {
        return LocalConverter.builder()
                .baseFolder(baseFolder)
                .processTimeout(2L, TimeUnit.MINUTES)
                .build();
    }

    public IConverter getConverter() {
        return converter;
    }

    public File getUploadFolder() {
        return uploadFolder;
    }

    public String nextFolderName() {
        return String.format("conversion%d", nameSequence.incrementAndGet());
    }
}
