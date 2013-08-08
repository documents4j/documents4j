package no.kantega.pdf.util;

import java.io.File;
import java.util.concurrent.Future;

public interface FileTransformationFuture<T> extends Future<T> {

    File getSource();

    File getTarget();
}
