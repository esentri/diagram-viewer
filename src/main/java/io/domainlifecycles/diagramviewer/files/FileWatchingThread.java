package io.domainlifecycles.diagramviewer.files;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class FileWatchingThread extends Thread {

    private final Callback callback;
    private final WatchService watchService;

    public FileWatchingThread(Callback callback, WatchService watchService) {
        this.callback = callback;
        this.watchService = watchService;
    }

    @Override
    public void run() {
        while (true) {
            WatchKey wk = null;
            try {
                wk = watchService.take();
                Thread.sleep(100); // give a chance for duplicate events to pile up
                for (WatchEvent<?> event : wk.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>)event;
                    callback.run(pathEvent);
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
            } finally {
                if (wk != null) {
                    wk.reset();
                }
            }
        }
    }
}
