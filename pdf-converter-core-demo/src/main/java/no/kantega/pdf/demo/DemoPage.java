package no.kantega.pdf.demo;

import no.kantega.pdf.job.IFileConsumer;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DemoPage extends WebPage {

    private static Logger LOGGER = LoggerFactory.getLogger(DemoPage.class);

    private final FileUploadField fileUploadField;

    public DemoPage(final PageParameters parameters) {
        super(parameters);

        add(new ColoredFeedbackPanel("feedback"));

        fileUploadField = makeUploadField("upload");

        Form<?> form = makeUploadForm("form");
        form.setMultiPart(true);
        form.add(fileUploadField);
        form.add(makeSubmitButton("submit"));
        add(form);

        add(makeResultView("results"));
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

                File newFolder = new File(DemoApplication.get().getUploadFolder(), DemoApplication.get().nextFolderName());
                newFolder.mkdirs();
                File newFile = new File(newFolder, fileUpload.getClientFileName());

                try {
                    newFile.createNewFile();
                    fileUpload.writeTo(newFile);
                    File target = new File(newFolder, fileUpload.getClientFileName() + ".pdf");
                    FeedbackMessageConductor conductor = new FeedbackMessageConductor();
                    DemoApplication.get().getConverter().convert(newFile, target, conductor);
                    getFeedbackMessages().add(conductor.getFeedbackMessage());

                } catch (Exception e) {
                    error(String.format("A web application error occurred. [%s: %s]", e.getClass().getSimpleName(), e.getMessage()));
                }

            }
        };
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

        @Override
        public void onComplete(File file) {
            String message = String.format("File '%s' was successfully converted.", file.getName());
            feedbackMessage = new FeedbackMessage(DemoPage.this, message, FeedbackMessage.SUCCESS);
            LOGGER.info(message);
        }

        @Override
        public void onCancel(File file) {
            String message = String.format("File conversion of '%s' was cancelled.", file.getName());
            feedbackMessage = new FeedbackMessage(DemoPage.this, message, FeedbackMessage.ERROR);
            LOGGER.error(message);
        }

        @Override
        public void onException(File file, Exception e) {
            String message = String.format("Could not convert file. [%s: %s]", e.getClass().getSimpleName(), e.getMessage());
            feedbackMessage = new FeedbackMessage(DemoPage.this, message, FeedbackMessage.ERROR);
            LOGGER.error(message, e);
        }

        private FeedbackMessage getFeedbackMessage() {
            return feedbackMessage;
        }
    }
}
