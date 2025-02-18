package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class UploadDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(UploadDialog.class);

    private final MultiFileMemoryBuffer uploadBuffer;
    private final Upload upload;

    @Value("${targets.location}")
    private String targetsLocation;

    public UploadDialog() {
        uploadBuffer = new MultiFileMemoryBuffer();
        upload = new Upload(uploadBuffer);

        upload.setMaxFileSize(50000000); // 50MB
        upload.setAcceptedFileTypes("jar");
        add(upload);
    }

    @PostConstruct
    private void addUploadComponent() {
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            log.debug(String.format("Trying to upload file '%s'...", fileName));

            if(targetsLocation == null || targetsLocation.isBlank()) {
                throw DiagramViewerException.fail("No upload location for targets specified.");
            }

            InputStream inputStream = uploadBuffer.getInputStream(fileName);

            try {
                FileIOUtils.saveFile(targetsLocation, inputStream, fileName);
            } catch (IOException e) {
                throw DiagramViewerException.fail(String.format("Couldn't upload file '%s' to '%s'.", fileName,
                    targetsLocation), e);
            }

            log.info(String.format("Successfully uploaded file '%s' to '%s'.", fileName, targetsLocation));
        });
    }
}
