package io.domainlifecycles.diagramviewer.files;

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileWatcher {

    private final static Logger log = LoggerFactory.getLogger(FileWatcher.class);

    private Thread thread;
    private WatchService watchService;

    /**
     * Starts watching a file and the given path and calls the callback when it is changed.
     * A shutdown hook is registered to stop watching. To control this yourself, create an
     * instance and use the start/stop methods.
     */
    public static FileWatcher onFileChange(Path file, Callback callback) throws IOException {
        FileWatcher fileWatcher = new FileWatcher();
        fileWatcher.start(file, callback);

        log.info(String.format("FileWatcher successfully started and watching directory '%s'", file));
        Runtime.getRuntime().addShutdownHook(new Thread(fileWatcher::stop));
        return fileWatcher;
    }

    private void start(Path file, Callback callback) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        file.register(
            watchService,
            new WatchEvent.Kind[]{
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE},
            SensitivityWatchEventModifier.HIGH
        );

        thread = new FileWatchingThread(callback, watchService);
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        try {watchService.close();}
        catch (IOException ignored) {}
    }
}
