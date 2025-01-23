package io.domainlifecycles.diagramviewer.files;

import java.io.IOException;
import java.nio.file.*;

public class DirectoryWatcher implements Runnable {

    private final Path directoryPath;
    private final DirectoryChangeListener listener;
    private boolean running = true;

    public DirectoryWatcher(String directoryPath, DirectoryChangeListener listener) {
        this.directoryPath = Paths.get(directoryPath);
        this.listener = listener;
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            while (running) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> ignored : key.pollEvents()) {
                    listener.onDirectoryChange();
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    public interface DirectoryChangeListener {
        void onDirectoryChange();
    }
}
