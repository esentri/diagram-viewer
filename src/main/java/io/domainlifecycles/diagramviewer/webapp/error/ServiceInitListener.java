package io.domainlifecycles.diagramviewer.webapp.error;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Component;

@Component
public class ServiceInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> {
            VaadinSession session = VaadinSession.getCurrent();
            if(session != null) session.setErrorHandler(new CustomErrorHandler());
        });
    }
}