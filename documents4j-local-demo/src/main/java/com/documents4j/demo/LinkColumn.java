package com.documents4j.demo;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import java.io.File;

class LinkColumn extends AbstractColumn<FileRow, Void> {

    private final FileInput fileInput;

    LinkColumn(IModel<String> displayModel, FileInput fileInput) {
        super(displayModel);
        this.fileInput = fileInput;
    }

    @Override
    public void populateItem(Item<ICellPopulator<FileRow>> cellItem, String componentId, IModel<FileRow> rowModel) {
        cellItem.add(new LinkPanel(componentId, new AlternateFileModel(rowModel), new AlternateFileFormatModel(rowModel), new AlternateFileNameModel(rowModel)));
    }

    public enum FileInput {
        SOURCE,
        TARGET
    }

    private class AlternateFileNameModel implements IModel<String> {

        private final IModel<FileRow> rowModel;

        private AlternateFileNameModel(IModel<FileRow> rowModel) {
            this.rowModel = rowModel;
        }

        @Override
        public String getObject() {
            return fileInput == FileInput.SOURCE ? rowModel.getObject().getSourceName() : rowModel.getObject().getOutputName();
        }
    }

    private class AlternateFileFormatModel implements IModel<String> {

        private final IModel<FileRow> rowModel;

        private AlternateFileFormatModel(IModel<FileRow> rowModel) {
            this.rowModel = rowModel;
        }

        @Override
        public String getObject() {
            return fileInput == FileInput.SOURCE ? rowModel.getObject().getSourceFormat() : rowModel.getObject().getTargetFormat();
        }
    }

    private class AlternateFileModel implements IModel<File> {

        private final IModel<FileRow> rowModel;

        private AlternateFileModel(IModel<FileRow> rowModel) {
            this.rowModel = rowModel;
        }

        @Override
        public File getObject() {
            return fileInput == FileInput.SOURCE ? rowModel.getObject().getSource() : rowModel.getObject().getTarget();
        }
    }
}
