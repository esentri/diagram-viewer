package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.domainlifecycles.diagramviewer.webapp.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

@Route(value = "/", layout = MainLayout.class)
@PageTitle("DLC | Home")
@PermitAll
public class DefaultView extends FlexLayout {

    public DefaultView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(getPageContents());
    }

    private VerticalLayout getPageContents() {
        VerticalLayout layout = new VerticalLayout();

        layout.add(new H2("Select a Project/Diagram on the left!"));

        layout.setAlignItems(Alignment.CENTER);
        return layout;
    }
}
