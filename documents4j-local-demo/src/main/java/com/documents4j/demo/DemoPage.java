package com.documents4j.demo;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IFileConsumer;
import com.documents4j.throwables.ConverterException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class DemoPage extends WebPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoPage.class);

    private static final CssResourceReference TWITTER_BOOTSTRAP = new CssResourceReference(DemoPage.class, "bootstrap.min.css");

    private final FileUploadField fileUploadField;
    private final DropDownChoice<DocumentType> sourceFormat, targetFormat;

    public DemoPage(PageParameters parameters) {
        super(parameters);

        add(new ColoredFeedbackPanel("feedback"));
        add(makeLocalWarning("localhost"));

        fileUploadField = makeUploadField("upload");
        final Map<DocumentType, Set<DocumentType>> conversions = DemoApplication.get().getConverter().getSupportedConversions();
        List<DocumentType> documentTypes = new ArrayList<DocumentType>(conversions.keySet());
        Collections.sort(documentTypes);
        sourceFormat = makeDropDownChoice("sourceFormat", new ListModel<DocumentType>(documentTypes));
        targetFormat = makeDropDownChoice("targetFormat", new IModel<List<DocumentType>>() {
            @Override
            public List<DocumentType> getObject() {
                if (sourceFormat.getModelObject() == null) {
                    return Collections.emptyList();
                }
                Set<DocumentType> sourceTypes = conversions.get(sourceFormat.getModelObject());
                if (sourceTypes == null) {
                    return Collections.emptyList();
                }
                List<DocumentType> documentTypes = new ArrayList<DocumentType>(sourceTypes);
                Collections.sort(documentTypes);
                return documentTypes;
            }
        });
        sourceFormat.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(targetFormat);
                }
            }
        });

        Form<?> form = makeUploadForm("form");
        form.setMultiPart(true);
        form.add(fileUploadField);
        form.add(sourceFormat);
        form.add(targetFormat);
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

                boolean abort = false;
                if (fileUpload == null) {
                    abort = true;
                    warn("You did not choose a file!");
                }
                if (sourceFormat.getModelObject() == null) {
                    abort = true;
                    warn("You did not choose a source format!");
                }
                if (targetFormat.getModelObject() == null) {
                    abort = true;
                    warn("You did not choose a target format!");
                }
                if (abort) {
                    return;
                }

                File transactionFolder = new File(DemoApplication.get().getUploadFolder(), DemoApplication.get().nextFolderName());
                if (!transactionFolder.mkdirs()) {
                    LOGGER.warn("Could not create directory", transactionFolder);
                }
                File newFile = new File(transactionFolder, FileRow.SOURCE_FILE_NAME);

                try {
                    if (!newFile.createNewFile()) {
                        throw new IOException(String.format("Could not create file %s", newFile));
                    }
                    fileUpload.writeTo(newFile);
                    File target = new File(transactionFolder, FileRow.TARGET_FILE_NAME);

                    Properties properties = new Properties();
                    properties.setProperty(FileRow.INPUT_NAME_PROPERTY_KEY, fileUpload.getClientFileName());
                    properties.setProperty(FileRow.SOURCE_FORMAT, sourceFormat.getModelObject().toString());
                    properties.setProperty(FileRow.TARGET_FORMAT, targetFormat.getModelObject().toString());

                    FeedbackMessageConductor conductor = new FeedbackMessageConductor(fileUpload.getClientFileName());
                    long conversionDuration;
                    try {
                        conversionDuration = System.currentTimeMillis();
                        DemoApplication.get().getConverter()
                                .convert(newFile).as(sourceFormat.getModelObject())
                                .to(target, conductor).as(targetFormat.getModelObject())
                                .execute();
                        conversionDuration = System.currentTimeMillis() - conversionDuration;
                        properties.setProperty(FileRow.CONVERSION_DURATION_PROPERTY_KEY, String.valueOf(conversionDuration));
                        writeProperties(properties, transactionFolder);
                    } catch (ConverterException e) {
                        File[] transactionFiles = transactionFolder.listFiles();
                        if (transactionFiles != null) {
                            for (File file : transactionFiles) {
                                if (!file.delete()) {
                                    LOGGER.warn("Could not delete transaction file {}", file);
                                }
                            }
                        }
                        if (!transactionFolder.delete()) {
                            LOGGER.warn("Could not delete transaction folder {}", transactionFolder);
                        }
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
        if (!file.createNewFile()) {
            throw new IOException(String.format("Could not create properties file %s", file));
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            properties.store(fileOutputStream, FileRow.PROPERTY_COMMENT);
        } finally {
            fileOutputStream.close();
        }
    }

    private FileUploadField makeUploadField(String identifier) {
        return new FileUploadField(identifier);
    }

    private DropDownChoice<DocumentType> makeDropDownChoice(String identifier, IModel<List<DocumentType>> types) {
        DropDownChoice<DocumentType> dropDownChoice = new DropDownChoice<DocumentType>(identifier, new Model<DocumentType>(), types);
        dropDownChoice.setOutputMarkupId(true);
        return dropDownChoice;
    }

    private Component makeSubmitButton(String identifier) {
        return new Button(identifier);
    }

    private Component makeResultView(String identifier) {
        return new FileTable(identifier);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(TWITTER_BOOTSTRAP));
    }

    private class FeedbackMessageConductor implements IFileConsumer {

        private final String inputName;
        private FeedbackMessage feedbackMessage;

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
}
