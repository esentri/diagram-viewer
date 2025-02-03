package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/upload", layout = MainView.class)
@PageTitle("DLC | Upload")
public class UploadView extends VerticalLayout {

    private final static Logger log = LoggerFactory.getLogger(UploadView.class);

    @Value("${upload.location}")
    private String uploadLocation;
    private final MultiFileMemoryBuffer uploadBuffer;
    private final Upload upload;

    public UploadView() {
        this.setSizeFull();
        this.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER );
        this.setAlignItems(Alignment.CENTER);

        uploadBuffer = new MultiFileMemoryBuffer();
        upload = new Upload(uploadBuffer);

        upload.setMaxFileSize(5000000);
        upload.setAcceptedFileTypes("image/svg+xml", "image/jpeg", "image/jpg", "image/png", ".nomnoml");
        add(upload);
    }

    @PostConstruct
    private void addUploadComponent() {
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            log.debug(String.format("Trying to upload file '%s'...", fileName));

            if(uploadLocation == null || uploadLocation.isBlank()) {
                throw DiagramViewerException.fail("No upload location for diagrams specified.");
            }

            InputStream inputStream = uploadBuffer.getInputStream(fileName);

            try {
                FileIOUtils.saveFile(uploadLocation, inputStream, fileName);
            } catch (IOException e) {
                throw DiagramViewerException.fail(String.format("Couldn't upload file '%s' to '%s'.", fileName, uploadLocation), e);
            }

            log.info(String.format("Successfully uploaded file '%s' to '%s'.", fileName, uploadLocation));
        });
    }
}
