package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import java.awt.*;

public class DirectoryPicker extends Upload {

    private final DomainModelSessionStorage sessionStorage;

    public DirectoryPicker(DomainModelSessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        this.setSizeFull();
        this.setId("directoryPicker");

        set(PropertyDescriptors.propertyWithDefault("webkitdirectory", ""), "");
        set(PropertyDescriptors.propertyWithDefault("multiple", ""), "");

        this.setUploadButton(new Button(new Icon("vaadin:folder-open")));
        this.setDropAllowed(false);
    }
}
