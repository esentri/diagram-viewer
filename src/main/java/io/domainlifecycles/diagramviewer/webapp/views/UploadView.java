package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "/upload", layout = MainView.class)
@PageTitle("DLC | Upload")
public class UploadView extends VerticalLayout {

    @Value("${diagrams.location}")
    private String diagramFolderLocation;
    private MultiFileMemoryBuffer uploadBuffer;
    private Upload upload;

    public UploadView() {
        this.setSizeFull();
        this.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER );
        this.setAlignItems(Alignment.CENTER);

        uploadBuffer = new MultiFileMemoryBuffer();
        upload = new Upload(uploadBuffer);
        add(upload);
    }

    @PostConstruct
    private void addUploadComponent() {
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = uploadBuffer.getInputStream(fileName);
            FileIOUtils.saveFile(diagramFolderLocation, inputStream, fileName);
        });
    }
}
