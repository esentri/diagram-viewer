package io.domainlifecycles.diagramviewer.webapp.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomErrorHandler implements ErrorHandler {
    @Override
    public void error(ErrorEvent errorEvent) {
        String errorMessage = errorEvent.getThrowable().getMessage();
        if(UI.getCurrent() != null) {
            UI.getCurrent().access(() -> Notification.show(errorMessage));
        }
    }
}