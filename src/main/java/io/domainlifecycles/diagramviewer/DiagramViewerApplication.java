package io.domainlifecycles.diagramviewer;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Push // Enable Vaadin Push to allow server side updates
public class DiagramViewerApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(DiagramViewerApplication.class, args);
    }
}
