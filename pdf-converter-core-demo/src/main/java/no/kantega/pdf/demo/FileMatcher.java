package no.kantega.pdf.demo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FileMatcher {

    private FileMatcher() {
        /* empty */
    }

    public static class FileRow implements Serializable {
        private final int row;
        private final File left;
        private final File right;

        public FileRow(int row, File left, File right) {
            this.row = row;
            this.left = left;
            this.right = right;
        }

        public File getLeft() {
            return left;
        }

        public File getRight() {
            return right;
        }

        public int getRow() {
            return row;
        }
    }

    public static List<FileRow> find() {

        File[] folders = DemoApplication.get().getUploadFolder().listFiles();
        Arrays.sort(folders);

        List<FileRow> result = new ArrayList<FileRow>();

        int rowcount = 0;
        for (File folder : folders) {

            File[] files = folder.listFiles();
            if (files.length != 2) {
                continue;
            }
            File first, second;
            if (files[0].getName().length() > files[1].getName().length()) {
                second = files[1];
                first = files[0];
            } else {
                second = files[0];
                first = files[1];
            }
            result.add(new FileRow(++rowcount, first, second));
        }

        return result;
    }


}
