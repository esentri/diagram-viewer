/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2019-2025 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.webapp.views;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import io.domainlifecycles.diagramviewer.webapp.events.UserRegisteredEvent;
import lombok.Getter;
import lombok.Setter;

@Route(value = RegisterView.VIEW_PATH, autoLayout = false)
@PageTitle("DLC | Register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public static final String VIEW_PATH = "/register";
    private static final String FORM_WIDTH = "22rem";

    private final SecurityService securityService;
    private final Binder<RegisterOptions> binder;
    private final RegisterOptions registerOptions;

    public RegisterView(SecurityService securityService) {
        this.securityService = securityService;
        this.binder = new Binder<>(RegisterOptions.class);
        this.registerOptions = new RegisterOptions();

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        add(createBackButton());
        addPageContents();

        binder.readBean(registerOptions);
    }

    private Button createBackButton() {
        Button backButton = new Button("← Back", e ->
            UI.getCurrent().navigate(SignInView.VIEW_PATH)
        );

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle()
            .set("position", "absolute")
            .set("top", "1rem")
            .set("left", "1rem")
            .set("cursor", "pointer");

        return backButton;
    }

    private void addPageContents() {
        H2 title = new H2("Create account");

        TextField firstName = new TextField("First name");
        firstName.setRequiredIndicatorVisible(true);
        firstName.setClearButtonVisible(true);
        firstName.setWidthFull();
        firstName.focus();

        binder.forField(firstName)
            .asRequired("First name is required.")
            .withConverter(RegisterView::trimToNull, s -> s == null ? "" : s,
                "First name is required.")
            .bind(RegisterOptions::getFirstName, RegisterOptions::setFirstName);

        TextField lastName = new TextField("Last name");
        lastName.setRequiredIndicatorVisible(true);
        lastName.setClearButtonVisible(true);
        lastName.setWidthFull();

        binder.forField(lastName)
            .asRequired("Last name is required.")
            .withConverter(RegisterView::trimToNull, s -> s == null ? "" : s,
                "Last name is required.")
            .bind(RegisterOptions::getLastName, RegisterOptions::setLastName);

        EmailField email = new EmailField("Email");
        email.setRequiredIndicatorVisible(true);
        email.setClearButtonVisible(true);
        email.setWidthFull();

        binder.forField(email)
            .asRequired("Email is required.")
            .withValidator(new EmailValidator("Please enter a valid email address."))
            .bind(RegisterOptions::getEmail, RegisterOptions::setEmail);

        PasswordField password = new PasswordField("Password");
        password.setRequiredIndicatorVisible(true);
        password.setMinLength(8);
        password.setHelperText("At least 8 characters.");
        password.setWidthFull();

        binder.forField(password)
            .asRequired("Password is required.")
            .withValidator(p -> p != null && p.length() >= 8,
                "Password must be at least 8 characters.")
            .bind(RegisterOptions::getPassword, RegisterOptions::setPassword);

        Button registerButton = createAndGetRegisterButton();

        VerticalLayout form = new VerticalLayout(firstName, lastName, email, password, registerButton);

        form.setWidth(FORM_WIDTH);
        form.setPadding(false);
        form.setSpacing(false);
        form.setAlignItems(Alignment.STRETCH);
        form.getStyle().set("gap", "0.6rem");

        add(title, form);
    }

    private Button createAndGetRegisterButton() {
        Button registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.getStyle().set("cursor", "pointer");
        registerButton.setEnabled(false);
        registerButton.addClickShortcut(Key.ENTER);

        binder.addStatusChangeListener(e -> registerButton.setEnabled(binder.isValid()));

        registerButton.addClickListener(e -> {
            binder.writeBeanIfValid(registerOptions);
            securityService.registerInternalUser(
                registerOptions.getEmail(),
                registerOptions.getFirstName(),
                registerOptions.getLastName(),
                registerOptions.getPassword()
            );

            UI.getCurrent().navigate(SignInView.VIEW_PATH);
            ComponentUtil.fireEvent(UI.getCurrent(), new UserRegisteredEvent(this, false));
        });

        return registerButton;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Getter
    private static class RegisterOptions {
        private String email;
        private String firstName;
        private String lastName;

        @Setter
        private String password;

        public void setEmail(String email) {
            this.email = trimToNull(email);
        }

        public void setFirstName(String firstName) {
            this.firstName = trimToNull(firstName);
        }

        public void setLastName(String lastName) {
            this.lastName = trimToNull(lastName);
        }
    }
}