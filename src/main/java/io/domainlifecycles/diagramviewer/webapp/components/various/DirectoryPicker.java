package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.upload.SucceededEvent;

public class DirectoryPicker extends Input {

    public DirectoryPicker(String defaultDirectoryValue) {
        this.setSizeFull();
        setType("file");
        setPlaceholder(defaultDirectoryValue);
        set(PropertyDescriptors.propertyWithDefault("webkitdirectory", ""), "");
        set(PropertyDescriptors.propertyWithDefault("multiple", ""), "");
        addListener(SucceededEvent.class, (ComponentEventListener<SucceededEvent>) event -> {
            System.out.println("Test");
        });
    }
}
