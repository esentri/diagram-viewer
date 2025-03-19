package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import io.domainlifecycles.diagramviewer.webapp.layout.MainView;

@Route(value = "/", layout = MainView.class)
@PageTitle("DLC | Home")
public class DefaultView extends FlexLayout {

    public DefaultView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(new H3("No diagram selected..."));
    }
}
