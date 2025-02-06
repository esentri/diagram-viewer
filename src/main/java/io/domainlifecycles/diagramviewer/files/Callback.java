package io.domainlifecycles.diagramviewer.files;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface Callback {
    void run(WatchEvent<Path> event) throws Exception;
}