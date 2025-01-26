package io.domainlifecycles.diagramviewer.webapp.components.upload;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;

public class DiagramUploadComponent extends Upload {

    @Value("${diagrams.location}")
    private String diagramFolderLocation;

    public DiagramUploadComponent() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        setReceiver(buffer);

        addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);

            FileIOUtils.saveFile(diagramFolderLocation, inputStream, fileName);
        });
    }
}
