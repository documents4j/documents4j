package no.kantega.pdf.demo;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Iterator;

public class FileDataProvider implements IDataProvider<FileRow>, ISortStateLocator {
    @Override
    public Iterator<? extends FileRow> iterator(long l, long l2) {
        return FileRow.findAll().subList((int) l, (int) l2).iterator();
    }

    @Override
    public long size() {
        return FileRow.findAll().size();
    }

    @Override
    public IModel<FileRow> model(FileRow fileRow) {
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
