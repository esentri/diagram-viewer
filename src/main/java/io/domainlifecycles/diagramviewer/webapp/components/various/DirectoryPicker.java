package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.component.upload.receivers.TemporaryFileFactory;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery.B;

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
