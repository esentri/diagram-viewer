package io.domainlifecycles.diagramviewer.webapp.session;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import io.domainlifecycles.diagramviewer.webapp.error.CustomErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceInitListener implements VaadinServiceInitListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(
                initEvent -> initEvent.getSession().setErrorHandler(new CustomErrorHandler()));
    }
}