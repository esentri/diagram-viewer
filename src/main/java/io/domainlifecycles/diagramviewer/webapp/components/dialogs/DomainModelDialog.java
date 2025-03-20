package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.domainlifecycles.diagramviewer.model.Diagram;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;

public class DomainModelDialog extends Dialog {

    private Button closeButton;
    private Diagram diagram;

    public DomainModelDialog(Diagram diagram) {
        this.diagram = diagram;
        setHeaderTitle("Domain Model");
        add(createDialogLayout());
        getFooter().add(createCloseButton());
    }

    private Button createCloseButton() {
        closeButton = new Button("Close", e -> close());
        return closeButton;
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        return formLayout;
    }
}
