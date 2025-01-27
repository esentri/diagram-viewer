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

    public UploadView() {
        this.setSizeFull();
        this.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER );
        this.setAlignItems(Alignment.CENTER);
    }

    @PostConstruct
    private void addUploadComponent() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload uploadComponent = new Upload(buffer);

        uploadComponent.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);
            FileIOUtils.saveFile(diagramFolderLocation, inputStream, fileName);
        });
        add(uploadComponent);
    }
}
