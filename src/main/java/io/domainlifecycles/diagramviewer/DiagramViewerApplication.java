package io.domainlifecycles.diagramviewer;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@StyleSheet(Lumo.STYLESHEET)
public class DiagramViewerApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(DiagramViewerApplication.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setViewport("width=device-width, initial-scale=1");
        settings.setPageTitle("DLC | Diagram Viewer");
        settings.addFavIcon("icon", "frontend/icons/favicon.png", "192x192");
        settings.addLink("shortcut icon", "frontend/icons/favicon.ico");
    }
}
