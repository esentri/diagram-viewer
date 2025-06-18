package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = SignInView.VIEW_PATH, autoLayout = false)
@PageTitle("DLC | Sign In")
@AnonymousAllowed
public class SignInView extends VerticalLayout {

    public static final String VIEW_PATH = "/signin";

    public SignInView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        setId("login");

        addPageContents();
    }


    private void addPageContents() {
        Image dlcImage = new Image("frontend/dlc-logo.png", "Domainlifecycles");
        dlcImage.setWidth("15%");
        dlcImage.getStyle().setMarginBottom("1rem");

        Image oktaImg = new Image("frontend/okta-logo.png", "Sign In With Okta");
        oktaImg.setHeight("calc(var(--_button-size)");

        Div imgWrapper = new Div(oktaImg);
        imgWrapper.getStyle().set("display", "flex");
        imgWrapper.getStyle().set("justify-content", "center");
        imgWrapper.getStyle().set("align-items", "center");
        imgWrapper.setWidthFull();

        Button signInButton = new Button(imgWrapper, e -> UI.getCurrent().getPage().setLocation("/oauth2/authorization/okta"));
        signInButton.setWidth("10%");
        signInButton.getStyle().set("cursor", "pointer");
        add(dlcImage, signInButton);
    }
}
