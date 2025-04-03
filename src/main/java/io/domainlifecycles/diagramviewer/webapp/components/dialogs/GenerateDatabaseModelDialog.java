package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.session.DomainModelSessionStorage;
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.Data;

public class GenerateDatabaseModelDialog extends Dialog {
    private static final String[] SQL_DIALECT_SELECT_VALUES = {"Oracle", "Postgres"};
    private static final String SQL_DDL_SCRIPT_SUFFIX = "-ddl-script.sql";
    private final Binder<GenerateDatabaseModelOptions> binder;

    private Button generateButton;
    private Checkbox auditModelCheckbox;
    private Select<String> sqlDialectSelect;
    private TextField bcPackageInput;
    private TextField bcSchemaInput;

    private final SQLDDLGeneratorService sqlDDLGeneratorService;
    private final DomainModelSessionStorage sessionStorage;

    public GenerateDatabaseModelDialog(SQLDDLGeneratorService sqlDDLGeneratorService, DomainModelSessionStorage sessionStorage) {
        this.sqlDDLGeneratorService = sqlDDLGeneratorService;
        this.sessionStorage = sessionStorage;
        this.binder = new Binder<>();

        setHeaderTitle("Download SQL-DDL-Model");
        add(createDialogLayout());
        getFooter().add(createGenerateButton());
        getFooter().add(createCloseButton());
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        auditModelCheckbox = new Checkbox();
        binder.forField(auditModelCheckbox).bind(GenerateDatabaseModelOptions::isAuditModel, GenerateDatabaseModelOptions::setAuditModel);

        sqlDialectSelect = new Select<>();
        sqlDialectSelect.setItems(SQL_DIALECT_SELECT_VALUES);
        sqlDialectSelect.setValue(SQL_DIALECT_SELECT_VALUES[0]);
        binder.forField(sqlDialectSelect).bind(GenerateDatabaseModelOptions::getSelectedSqlDialect, GenerateDatabaseModelOptions::setSelectedSqlDialect);

        bcPackageInput = new TextField();
        binder.forField(bcPackageInput)
            .asRequired("Package may not be empty")
            .bind(GenerateDatabaseModelOptions::getBoundedContextPackageName, GenerateDatabaseModelOptions::setBoundedContextPackageName);

        bcSchemaInput = new TextField();
        binder.forField(bcSchemaInput)
            .asRequired("Schema may not be empty")
            .bind(GenerateDatabaseModelOptions::getBoundedContextPackageSchemaName, GenerateDatabaseModelOptions::setBoundedContextPackageSchemaName);

        binder.addStatusChangeListener(event -> generateButton.setEnabled(binder.isValid()));

        formLayout.addFormItem(bcPackageInput, "Bounded Context Package name");
        formLayout.addFormItem(bcSchemaInput, "Bounded Context name");
        formLayout.addFormItem(sqlDialectSelect,"SQL Dialect");
        formLayout.addFormItem(auditModelCheckbox, "Audit Model");
        return formLayout;
    }

    private Button createGenerateButton() {
        StreamResource streamResource = new StreamResource(buildScriptFilename(), this::getStream);
        Anchor generateLink = new Anchor(streamResource, "Download SQL-Script");
        generateLink.getElement().setAttribute("download", true);
        generateLink.setId("sql-generate-link");

        generateButton = new Button(generateLink);
        generateButton.setEnabled(binder.isValid());
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateButton.addClickListener(event -> close());

        return generateButton;
    }

    private String buildScriptFilename() {
        Project project = sessionStorage.getSelectedProject();
        return project == null ? "dlc-project" + SQL_DDL_SCRIPT_SUFFIX : project.getProjectNameClean() + SQL_DDL_SCRIPT_SUFFIX;
    }

    private Button createCloseButton() {
        return new Button("Close", e -> close());
    }

    private InputStream getStream() {
        final String ddl = sqlDDLGeneratorService.generateSQL(
            sessionStorage.get(sessionStorage.getSelectedProject().getProjectId()),
            bcPackageInput.getValue(), bcSchemaInput.getValue(), sqlDialectSelect.getValue(), auditModelCheckbox.getValue());

        return new ByteArrayInputStream(ddl.getBytes(StandardCharsets.UTF_8));
    }

    @Data
    private static class GenerateDatabaseModelOptions {
        private String boundedContextPackageName;
        private String boundedContextPackageSchemaName;
        private String selectedSqlDialect;
        private boolean auditModel;
    }
}
