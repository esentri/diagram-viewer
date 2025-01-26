package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.util.FileIOUtils;
import io.domainlifecycles.diagramviewer.webapp.components.upload.DiagramUploadComponent;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Route(value = "/upload", layout = MainView.class)
@PageTitle("DLC | Upload")
@Service
public class UploadView extends VerticalLayout {

    public UploadView() {
        this.setSizeFull();
        this.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER );
        this.setAlignItems(Alignment.CENTER);

        add(new DiagramUploadComponent());
    }
}
