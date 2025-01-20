package io.domainlifecycles.diagramviewer;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiagramViewerApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(DiagramViewerApplication.class, args);
    }
}
