package com.documents4j.demo;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

class FileTable extends DataTable<FileRow, Void> {

    @SuppressWarnings("unchecked")
    public FileTable(String id) {
        super(id, makeColumns(), makeDataProvider(), 100L);
        addTopToolbar(new HeadersToolbar<Void>(this, (ISortStateLocator<Void>) getDataProvider()));
        addBottomToolbar(new NoRecordsToolbar(this));
    }

    private static List<? extends IColumn<FileRow, Void>> makeColumns() {
        List<IColumn<FileRow, Void>> result = new ArrayList<IColumn<FileRow, Void>>();
        result.add(new PropertyColumn<FileRow, Void>(Model.of("#"), "row"));
        result.add(new LinkColumn(Model.of("Uploaded file"), LinkColumn.FileInput.SOURCE));
        result.add(new LinkColumn(Model.of("Converted file"), LinkColumn.FileInput.TARGET));
        result.add(new PropertyColumn<FileRow, Void>(Model.of("Duration"), "duration"));
        return result;
    }

    private static IDataProvider<FileRow> makeDataProvider() {
        return new FileDataProvider();
    }
}
