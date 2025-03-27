package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.domainlifecycles.diagramviewer.webapp.components.various.SignInWithGoogleButton;

@Route(value = "/login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    public LoginView() {
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        add(getPageContents());
    }

    private VerticalLayout getPageContents() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setId("login-box");
        verticalLayout.setHeight("50%");
        verticalLayout.setWidth("20%");

        verticalLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        verticalLayout.setAlignItems(Alignment.CENTER);

        verticalLayout.add(new H2("DLC | Diagram Viewer"), new SignInWithGoogleButton());

        return verticalLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
            .getQueryParameters()
            .getParameters()
            .containsKey("error")) {
            add(new Paragraph("Error"));
        }
    }
}
