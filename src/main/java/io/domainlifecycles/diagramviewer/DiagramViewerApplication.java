package io.domainlifecycles.diagramviewer;

import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.component.page.AppShellConfigurator;

@SpringBootApplication
@Viewport("width=device-width, initial-scale=1")
public class DiagramViewerApplication implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
    }

    public static void main(String[] args) {
        SpringApplication.run(DiagramViewerApplication.class, args);

    }
}
