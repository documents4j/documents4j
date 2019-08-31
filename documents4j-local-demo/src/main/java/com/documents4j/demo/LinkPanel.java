package com.documents4j.demo;

import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.File;

class LinkPanel extends Panel {

    LinkPanel(String id, IModel<File> file, IModel<String> type, IModel<String> name) {
        super(id);
        add(new DownloadLink("file", file, name).setBody(new FileNameAndFormatAndSizeModel(file, type, name)));
    }

    private static class FileNameAndFormatAndSizeModel implements IModel<String> {

        private final IModel<File> fileModel;
        private final IModel<String> typeModel;
        private final IModel<String> nameModel;

        private FileNameAndFormatAndSizeModel(IModel<File> fileModel, IModel<String> typeModel, IModel<String> nameModel) {
            this.fileModel = fileModel;
            this.typeModel = typeModel;
            this.nameModel = nameModel;
        }

        @Override
        public String getObject() {
            return String.format("%s (%s, %s)", nameModel.getObject(), typeModel.getObject(), prettySize());
        }

        private String prettySize() {
            long size = fileModel.getObject().length();
            if (size < 1024L) {
                return String.format("%d byte", size);
            } else if (size < 1024L * 1024L) {
                return String.format("~%d kB", size / 1024L);
            } else {
                // Integral division is wanted in order to truncate the resulting value.
                double truncatedMegabyte = (size / 1024L) / 1024d;
                return String.format("~%.2f MB", truncatedMegabyte);
            }
        }
    }
}
