package no.kantega.pdf.demo;

import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import java.io.File;

public class LinkPanel extends Panel {

    LinkPanel(String id, IModel<File> file, IModel<String> name) {
        super(id);
        add(new DownloadLink("file", file, name).setBody(new FileNameAndSizeModel(file, name)));
    }

    private static class FileNameAndSizeModel extends AbstractReadOnlyModel<String> {

        private final IModel<File> fileModel;
        private final IModel<String> nameModel;

        private FileNameAndSizeModel(IModel<File> fileModel, IModel<String> nameModel) {
            this.fileModel = fileModel;
            this.nameModel = nameModel;
        }

        @Override
        public String getObject() {
            return String.format("%s (%s)", nameModel.getObject(), prettySize());
        }

        private String prettySize() {
            long size = fileModel.getObject().length();
            if (size < 1024L) {
                return String.format("%d byte", size);
            } else if (size < 1024L * 1024L) {
                return String.format("~%d kB", size / 1024L);
            } else {
                double truncatedMegabyte = (size / 1024L) / 1024d;
                return String.format("~%.2f MB", truncatedMegabyte);
            }
        }
    }
}
