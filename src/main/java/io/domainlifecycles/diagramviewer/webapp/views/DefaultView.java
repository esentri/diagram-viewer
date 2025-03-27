package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

@Route(value = "/", layout = MainLayout.class)
@PageTitle("DLC | Home")
@AnonymousAllowed
public class DefaultView extends FlexLayout {

    public DefaultView(DomainModelSessionStorage sessionStorage) {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(getPageContents());

        sessionStorage.setNoneSelected();
    }

    private VerticalLayout getPageContents() {
        VerticalLayout layout = new VerticalLayout();

        layout.add(new H3("Please select a diagram on the left."));
        layout.add(new H3("You can upload a new .jar-file in the bottom left corner and create a new diagram by clicking on the + icon right next to your .jar-file"));

        layout.setAlignItems(Alignment.CENTER);
        return layout;
    }
}
