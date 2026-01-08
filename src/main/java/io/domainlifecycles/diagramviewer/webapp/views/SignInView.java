package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import io.domainlifecycles.diagramviewer.webapp.events.UserRegisteredEvent;

@Route(value = SignInView.VIEW_PATH, autoLayout = false)
@PageTitle("DLC | Login")
@AnonymousAllowed
public class SignInView extends VerticalLayout implements BeforeEnterObserver {

    public static final String VIEW_PATH = "/signin";

    private static final String BUTTON_WIDTH = "22rem";
    private Button backButton;
    private LoginOverlay loginOverlay;
    private Registration registration;

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
        dlcImage.getStyle().set("margin-bottom", "1.5rem");

        LoginOverlay loginOverlay = getLoginOverlay();

        Button loginButton = new Button("Log in");
        loginButton.setWidth(BUTTON_WIDTH);
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle().set("cursor", "pointer");
        loginButton.addClickListener(e -> {
            openLogin(true);
        });

        Button registerButton = new Button("Create account");
        registerButton.setWidth(BUTTON_WIDTH);
        registerButton.getStyle().set("cursor", "pointer");
        registerButton.addClickListener(e -> UI.getCurrent().navigate("/register"));

        Button oktaButton = getSignInWithOktaButton();
        oktaButton.setWidth(BUTTON_WIDTH);
        oktaButton.getStyle().set("cursor", "pointer");

        VerticalLayout buttons = new VerticalLayout(
            loginButton,
            registerButton,
            getOrDivider(BUTTON_WIDTH),
            oktaButton
        );
        buttons.setPadding(false);
        buttons.setSpacing(false);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.getStyle().set("gap", "0.5rem");

        add(dlcImage, loginOverlay, buttons);
    }

    private void openLogin(boolean open) {
        loginOverlay.setOpened(open);
        backButton.setVisible(open);
    }

    private LoginOverlay getLoginOverlay() {
        loginOverlay = new LoginOverlay();
        loginOverlay.setAction("login");
        loginOverlay.setForgotPasswordButtonVisible(false);
        loginOverlay.getFooter().add(getBackButton());
        return loginOverlay;
    }

    private Button getSignInWithOktaButton() {
        Image oktaImg = new Image("frontend/okta.svg", "Log in with Okta");
        oktaImg.setHeight("1.125rem");

        HorizontalLayout content = new HorizontalLayout(oktaImg, new Text("Continue with Okta"));
        content.setAlignItems(Alignment.CENTER);
        content.setSpacing(true);
        content.setWidthFull();
        content.setJustifyContentMode(JustifyContentMode.CENTER);

        Button button = new Button(content, e ->
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/okta")
        );
        button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        button.getStyle().set("cursor", "pointer");
        return button;
    }

    private Button getBackButton() {
        backButton = new Button("← Back", e -> openLogin(false));

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle()
            .set("position", "absolute")
            .set("top", "1rem")
            .set("left", "1rem")
            .set("cursor", "pointer");

        return backButton;
    }

    private HorizontalLayout getOrDivider(String width) {
        HorizontalLayout divider = new HorizontalLayout();
        divider.setWidth(width);
        divider.setAlignItems(Alignment.CENTER);
        divider.setSpacing(true);

        Div left = new Div();
        left.getStyle()
            .set("height", "1px")
            .set("background", "var(--lumo-contrast-20pct)")
            .set("flex-grow", "1");

        Div right = new Div();
        right.getStyle()
            .set("height", "1px")
            .set("background", "var(--lumo-contrast-20pct)")
            .set("flex-grow", "1");

        Div text = new Div(new Text("or"));
        text.getStyle()
            .set("padding", "0 0.5rem")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("line-height", "1");

        divider.add(left, text, right);
        return divider;
    }

    private boolean hasLoginError(BeforeEnterEvent event) {
        return event.getLocation().getQueryParameters().getParameters().containsKey("error");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration = ComponentUtil.addListener(
            attachEvent.getUI(),
            UserRegisteredEvent.class,
            event -> openLogin(true)
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if(hasLoginError(event)) {
            openLogin(true);
            loginOverlay.setError(true);
        }
    }
}