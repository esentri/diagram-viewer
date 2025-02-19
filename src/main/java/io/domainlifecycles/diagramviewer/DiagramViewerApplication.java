package io.domainlifecycles.diagramviewer;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import io.domainlifecycles.diagramviewer.kroki.KrokiClient;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Push // Enable Vaadin Push to allow server side updates
public class DiagramViewerApplication implements AppShellConfigurator {

    @Autowired
    KrokiClient krokiClient;

    public static void main(String[] args) {
        SpringApplication.run(DiagramViewerApplication.class, args);
    }

    @PreDestroy
    void onExit() {
        krokiClient.finish();
    }
}
