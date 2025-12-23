package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.html.Image;
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

        Image dlcLogo = new Image("frontend/icons/favicon.png", "dlc-logo");
        dlcLogo.setHeight("15%");
        dlcLogo.setWidth("15%");
        layout.add(dlcLogo);

        layout.setAlignItems(Alignment.CENTER);
        return layout;
    }
}
