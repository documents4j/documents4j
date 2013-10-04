package no.kantega.pdf.demo;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileTable extends DataTable<FileMatcher.FileRow, Void> {

    @SuppressWarnings("unchecked")
    public FileTable(String id) {
        super(id, makeColumns(), makeDataProvider(), 100L);
        addTopToolbar(new HeadersToolbar<Void>(this, (ISortStateLocator<Void>) getDataProvider()));
        addBottomToolbar(new NoRecordsToolbar(this));
    }

    private static List<? extends IColumn<FileMatcher.FileRow, Void>> makeColumns() {
        List<IColumn<FileMatcher.FileRow, Void>> result = new ArrayList<IColumn<FileMatcher.FileRow, Void>>();
        result.add(new PropertyColumn<FileMatcher.FileRow, Void>(Model.of("Number"), "row"));
        result.add(new LinkColumn(Model.of("Upload"), LinkColumn.Side.RIGHT));
        result.add(new LinkColumn(Model.of("Result"), LinkColumn.Side.LEFT));
        return result;
    }

    private static IDataProvider<FileMatcher.FileRow> makeDataProvider() {
        return new FileDataProvider();
    }
}

class FileDataProvider implements IDataProvider<FileMatcher.FileRow>, ISortStateLocator {
    @Override
    public Iterator<? extends FileMatcher.FileRow> iterator(long l, long l2) {
        return FileMatcher.find().subList((int) l, (int) l2).iterator();
    }

    @Override
    public long size() {
        return FileMatcher.find().size();
    }

    @Override
    public IModel<FileMatcher.FileRow> model(FileMatcher.FileRow fileRow) {
        return Model.of(fileRow);
    }

    @Override
    public void detach() {
        /* empty */
    }

    @Override
    public ISortState getSortState() {
        return new ISortState() {
            @Override
            public void setPropertySortOrder(Object property, SortOrder order) {
                /* empty */
            }

            @Override
            public SortOrder getPropertySortOrder(Object property) {
                return SortOrder.NONE;
            }
        };
    }
}

class LinkColumn extends AbstractColumn<FileMatcher.FileRow, Void> {

    public enum Side {
        LEFT,
        RIGHT
    }

    private final Side side;

    LinkColumn(IModel<String> displayModel, Side side) {
        super(displayModel);
        this.side = side;
    }

    @Override
    public void populateItem(Item<ICellPopulator<FileMatcher.FileRow>> cellItem,
                             String componentId, IModel<FileMatcher.FileRow> rowModel) {
        cellItem.add(new LinkPanel(componentId, new AlternateFileModel(rowModel)));
    }

    private class AlternateFileModel extends AbstractReadOnlyModel<File> {

        private final IModel<FileMatcher.FileRow> rowModel;

        private AlternateFileModel(IModel<FileMatcher.FileRow> rowModel) {
            this.rowModel = rowModel;
        }

        @Override
        public File getObject() {
            return side == Side.LEFT ? rowModel.getObject().getLeft() : rowModel.getObject().getRight();
        }
    }
}

class LinkPanel extends Panel {

    LinkPanel(String id, IModel<File> file) {
        super(id);
        add(new DownloadLink("file", file).setBody(new PropertyModel<String>(file, "name")));
    }
}
