package no.kantega.pdf.demo;

import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.throwables.ConversionException;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DemoPage extends WebPage {

    private static Logger LOGGER = LoggerFactory.getLogger(DemoPage.class);

    private static final CssResourceReference TWITTER_BOOTSTRAP = new CssResourceReference(DemoPage.class, "bootstrap.min.css");

    private final FileUploadField fileUploadField;

    public DemoPage(final PageParameters parameters) {
        super(parameters);

        add(new ColoredFeedbackPanel("feedback"));
        add(makeLocalWarning("localhost"));

        fileUploadField = makeUploadField("upload");

        Form<?> form = makeUploadForm("form");
        form.setMultiPart(true);
        form.add(fileUploadField);
        form.add(makeSubmitButton("submit"));
        add(form);

        add(makeResultView("results"));
    }

    private Component makeLocalWarning(String identifier) {
        Component warning = new WebMarkupContainer(identifier);
        String host = getRequestCycle().getRequest().getClientUrl().getHost();
        if (!host.equals("localhost") && !host.equals("127.0.0.1")) {
            warning.setVisible(false);
        }
        return warning;
    }

    private Form<?> makeUploadForm(String identifier) {
        return new Form<Void>(identifier) {

            @Override
            protected void onSubmit() {

                FileUpload fileUpload = fileUploadField.getFileUpload();

                if (fileUpload == null) {
                    warn("You did not choose a file!");
                    return;
                }

                File transactionFolder = new File(DemoApplication.get().getUploadFolder(), DemoApplication.get().nextFolderName());
                transactionFolder.mkdirs();
                File newFile = new File(transactionFolder, FileRow.SOURCE_FILE_NAME);

                try {
                    newFile.createNewFile();
                    fileUpload.writeTo(newFile);
                    File target = new File(transactionFolder, FileRow.TARGET_FILE_NAME);

                    Properties properties = new Properties();
                    properties.setProperty(FileRow.INPUT_NAME_PROPERTY_KEY, fileUpload.getClientFileName());

                    FeedbackMessageConductor conductor = new FeedbackMessageConductor(fileUpload.getClientFileName());
                    long conversionDuration = -1L;
                    try {
                        conversionDuration = System.currentTimeMillis();
                        DemoApplication.get().getConverter().convert(newFile, target, conductor);
                        conversionDuration = System.currentTimeMillis() - conversionDuration;
                        properties.setProperty(FileRow.CONVERSION_DURATION_PROPERTY_KEY, String.valueOf(conversionDuration));
                        writeProperties(properties, transactionFolder);
                    } catch (ConversionException e) {
                        transactionFolder.delete();
                        /* other than this, this exception is already handled by the callback (FeedbackMessageConductor) */
                    }
                    getFeedbackMessages().add(conductor.getFeedbackMessage());

                } catch (Exception e) {
                    error(String.format("A web application error occurred. [%s: %s]", e.getClass().getSimpleName(), e.getMessage()));
                }

            }
        };
    }

    private void writeProperties(Properties properties, File transactionFolder) throws IOException {
        File file = new File(transactionFolder, FileRow.PROPERTIES_FILE_NAME);
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        properties.store(fileOutputStream, FileRow.PROPERTY_COMMENT);
    }

    private FileUploadField makeUploadField(String identifier) {
        return new FileUploadField(identifier);
    }

    private Component makeSubmitButton(String identifier) {
        return new Button(identifier);
    }

    private Component makeResultView(String identifier) {
        return new FileTable(identifier);
    }

    private class FeedbackMessageConductor implements IFileConsumer {

        private FeedbackMessage feedbackMessage;

        private final String inputName;

        private FeedbackMessageConductor(String inputName) {
            this.inputName = inputName;
        }

        @Override
        public void onComplete(File file) {
            String message = String.format("File '%s' was successfully converted.", inputName);
            feedbackMessage = new FeedbackMessage(DemoPage.this, message, FeedbackMessage.SUCCESS);
            LOGGER.info(message);
        }

        @Override
        public void onCancel(File file) {
            String message = String.format("File conversion of '%s' was cancelled.", inputName);
            feedbackMessage = new FeedbackMessage(DemoPage.this, message, FeedbackMessage.ERROR);
            LOGGER.error(message);
        }

        @Override
        public void onException(File file, Exception e) {
            String message = String.format("Could not convert file '%s'. Did you provide a valid MS Word file as input? [%s: %s]",
                    inputName, e.getClass().getSimpleName(), e.getMessage());
            feedbackMessage = new FeedbackMessage(DemoPage.this, message, FeedbackMessage.ERROR);
            LOGGER.error(message, e);
        }

        private FeedbackMessage getFeedbackMessage() {
            return feedbackMessage;
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(TWITTER_BOOTSTRAP));
    }
}
