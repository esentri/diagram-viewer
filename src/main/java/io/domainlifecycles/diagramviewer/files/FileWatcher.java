package io.domainlifecycles.diagramviewer.files;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatcher {

    private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);
    private Thread thread;
    private WatchService watchService;

    public interface Callback {
        void run() throws Exception;
    }

    /**
     * Starts watching a file and the given path and calls the callback when it is changed.
     * A shutdown hook is registered to stop watching. To control this yourself, create an
     * instance and use the start/stop methods.
     */
    public static void onFileChange(Path file, Callback callback) throws IOException {
        FileWatcher fileWatcher = new FileWatcher();
        fileWatcher.start(file, callback);
        Runtime.getRuntime().addShutdownHook(new Thread(fileWatcher::stop));
    }

    public void start(Path file, Callback callback) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        file.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

        thread = new Thread(() -> {
            while (true) {
                WatchKey wk = null;
                try {
                    wk = watchService.take();
                    Thread.sleep(500); // give a chance for duplicate events to pile up
                    for (WatchEvent<?> ignored : wk.pollEvents()) {
                        callback.run();
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                } finally {
                    if (wk != null) {
                        wk.reset();
                    }
                }
            }
        });
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        try {
            watchService.close();
        } catch (IOException e) {
        }
    }

}
