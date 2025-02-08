package io.domainlifecycles.diagramviewer.files;

import com.sun.nio.file.SensitivityWatchEventModifier;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher {

    private final static Logger log = LoggerFactory.getLogger(DirectoryWatcher.class);

    private DirectoryWatchingThread directoryWatchingThread;
    private WatchService watchService;

    /**
     * Starts watching a directory of the given path and calls the callback when it is changed.
     * A shutdown hook is registered to stop watching. To control this yourself, create an
     * instance and use the start/stop methods.
     */
    public static DirectoryWatcher onDirectoryChange(Path directory, Callback callback) {
        DirectoryWatcher directoryWatcher = new DirectoryWatcher();
        directoryWatcher.start(directory, callback);

        log.info(String.format("DirectoryWatcher successfully started and watching directory '%s'", directory));
        Runtime.getRuntime().addShutdownHook(new Thread(directoryWatcher::stop));
        return directoryWatcher;
    }

    private void start(Path directory, Callback callback) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(
                watchService,
                new WatchEvent.Kind[]{
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE},
                SensitivityWatchEventModifier.HIGH
            );
        } catch(NoSuchFileException | NotDirectoryException e) {
            return;
        } catch(IOException e) {
            throw DiagramViewerException.fail(
                String.format("Could not start watching directory '%s'.", directory), e);
        }

        directoryWatchingThread = new DirectoryWatchingThread(callback, watchService);
        directoryWatchingThread.start();
    }

    public void stop() {
        directoryWatchingThread.interrupt();
        try {watchService.close();}
        catch (IOException ignored) {}
    }
}
