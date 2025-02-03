package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import io.domainlifecycles.diagramviewer.generate.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.session.AnalyzedDomainModel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class GenerateDatabaseModelDialog extends Dialog {
    private static final String[] SQL_DIALECT_SELECT_VALUES = {"Oracle", "Postgres"};

    private Button closeButton;
    private Anchor generateLink;
    private Checkbox auditModelCheckbox;
    private Select<String> sqlDialectSelect;
    private TextArea output;
    TextField bcPackageInput;

    private SQLDDLGeneratorService sqlDDLGeneratorService;
    private AnalyzedDomainModel analyzedDomainModel;

    public GenerateDatabaseModelDialog(SQLDDLGeneratorService sqlDDLGeneratorService, AnalyzedDomainModel analyzedDomainModel) {
        this.sqlDDLGeneratorService = sqlDDLGeneratorService;
        this.analyzedDomainModel = analyzedDomainModel;
        setHeaderTitle("Download SQL DDL Model");
        add(createDialogLayout());
        getFooter().add(createGenerateLink());
        getFooter().add(createCloseButton());
    }

    private Anchor createGenerateLink() {
        StreamResource streamResource = new StreamResource("ddl.sql", this::getStream);
        generateLink = new Anchor(streamResource, "Download DDL SQL");
        generateLink.getElement().setAttribute("download", true);
        if(analyzedDomainModel.getDomainModel()== null){
            generateLink.setEnabled(false);
        }
        return generateLink;
    }

    private Button createCloseButton() {
        closeButton = new Button("Close", e -> close());
        return closeButton;
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();
        auditModelCheckbox = new Checkbox("Audit Model");
        sqlDialectSelect = new Select<>();
        sqlDialectSelect.setItems(SQL_DIALECT_SELECT_VALUES);
        sqlDialectSelect.setValue(SQL_DIALECT_SELECT_VALUES[0]);
        bcPackageInput = new TextField();
        bcPackageInput.setRequiredIndicatorVisible(true);
        bcPackageInput.setRequired(true);
        bcPackageInput.setErrorMessage("Please enter the package name");
        output = new TextArea();
        output.setSizeFull();
        output.setEnabled(false);
        formLayout.addFormItem(bcPackageInput, "Bounded context package");
        formLayout.addFormItem(sqlDialectSelect,"SQL Dialect");
        formLayout.addFormItem(auditModelCheckbox, "Audit Model");
        formLayout.addFormItem(output, "Output");
        return formLayout;
    }

    private InputStream getStream() {
        var ddl = sqlDDLGeneratorService.generateSQL(analyzedDomainModel.getDomainModel(), bcPackageInput.getValue(), sqlDialectSelect.getValue());
        output.setValue(ddl);
        return new ByteArrayInputStream(ddl.getBytes(StandardCharsets.UTF_8));
    }
}
