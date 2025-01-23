package io.domainlifecycles.diagramviewer.files;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class DirectoryMonitorService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DirectoryWatcher directoryWatcher;

    public void startMonitoring(String directoryPath, DirectoryWatcher.DirectoryChangeListener listener) {
        if (directoryWatcher != null) {
            stopMonitoring();
        }
        directoryWatcher = new DirectoryWatcher(directoryPath, listener);
        executor.submit(directoryWatcher);
    }

    public void stopMonitoring() {
        if (directoryWatcher != null) {
            directoryWatcher.stop();
        }
        executor.shutdownNow();
    }
}
