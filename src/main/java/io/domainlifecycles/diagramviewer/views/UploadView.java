package io.domainlifecycles.diagramviewer.views;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.layout.MainView;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Route(value = "/upload", layout = MainView.class)
@PageTitle("DLC | Upload")
@Service
public class UploadView extends VerticalLayout {

    @Value("${diagrams.location}")
    private String diagramFolderLocation;

    public UploadView() {
        this.setSizeFull();
        this.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER );
        this.setAlignItems(Alignment.CENTER);

        add(getUpload());
    }

    private Upload getUpload() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);

            saveFile(inputStream, fileName);
        });

        return upload;
    }

    private void saveFile(InputStream inputStream, String fileName) {
        final Path filePath = Path.of(diagramFolderLocation, fileName);

        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw DiagramViewerException.fail(String.format("Error occurred while trying to save file to %s.", filePath), e);
        }
    }
}
