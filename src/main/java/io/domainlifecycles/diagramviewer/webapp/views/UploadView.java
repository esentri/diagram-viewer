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

    private final String targetsLocation;
    private final MultiFileMemoryBuffer uploadBuffer;
    private final Upload upload;

    public UploadView(@Value("${targets.location}") String targetsLocation) {
        this.setSizeFull();
        this.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER );
        this.setAlignItems(Alignment.CENTER);

        this.targetsLocation = targetsLocation;
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
