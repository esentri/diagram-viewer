package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.domainlifecycles.diagramviewer.model.Project;
import io.domainlifecycles.diagramviewer.sql.SQLDDLGeneratorService;
import io.domainlifecycles.mirror.api.BoundedContextMirror;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;

public class GenerateDatabaseModelDialog extends Dialog {
    private static final String[] SQL_DIALECT_SELECT_VALUES = {"Oracle", "Postgres"};
    private static final String SQL_DDL_SCRIPT_SUFFIX = "-ddl-script.sql";
    private static final String APPLICATION_SQL_MIME_TYPE = "application/sql";

    private final Binder<GenerateDatabaseModelOptions> binder;
    private final SQLDDLGeneratorService sqlDDLGeneratorService;
    private final Project project;
    private final GenerateDatabaseModelOptions generateDatabaseModelOptions;

    private Button generateButton;

    public GenerateDatabaseModelDialog(SQLDDLGeneratorService sqlDDLGeneratorService, Project project) {
        this.sqlDDLGeneratorService = sqlDDLGeneratorService;
        this.project = project;
        this.binder = new Binder<>();

        generateDatabaseModelOptions = new GenerateDatabaseModelOptions();

        setHeaderTitle("Download SQL-DDL-Model");
        getFooter().add(createGenerateButton());
        getFooter().add(createCloseButton());
        add(createDialogLayout());

        binder.addStatusChangeListener(event -> generateButton.setEnabled(binder.isValid()));
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        Select<String> sqlDialectSelect = new Select<>();
        sqlDialectSelect.setItems(SQL_DIALECT_SELECT_VALUES);
        sqlDialectSelect.setValue(SQL_DIALECT_SELECT_VALUES[0]);
        binder.forField(sqlDialectSelect)
            .bind(GenerateDatabaseModelOptions::getSelectedSqlDialect, GenerateDatabaseModelOptions::setSelectedSqlDialect);
        formLayout.addFormItem(sqlDialectSelect, "SQL Dialect");

        Select<String> boundedContextPackageSelect = new Select<>();
        boundedContextPackageSelect.setItems(mapPackageNames());
        binder.forField(boundedContextPackageSelect)
            .asRequired("Context-Package is required.")
            .bind(GenerateDatabaseModelOptions::getBoundedContextPackage, GenerateDatabaseModelOptions::setBoundedContextPackage);
        formLayout.addFormItem(boundedContextPackageSelect, "Context-Package");

        Checkbox auditModelCheckbox = new Checkbox();
        binder.forField(auditModelCheckbox).bind(GenerateDatabaseModelOptions::isAuditModel, GenerateDatabaseModelOptions::setAuditModel);
        formLayout.addFormItem(auditModelCheckbox, "Audit Model");

        return formLayout;
    }

    private Button createGenerateButton() {
        generateButton = new Button("Download SQL-Script");
        generateButton.setEnabled(binder.isValid());
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        generateButton.addClickListener(event -> {
            binder.writeBeanIfValid(generateDatabaseModelOptions);
            String scriptFilename = buildScriptFilename();
            StreamResource streamResource = new StreamResource(scriptFilename, this::getStream);
            streamResource.setContentType(APPLICATION_SQL_MIME_TYPE);
            streamResource.setCacheTime(0);

            Anchor tempLink = new Anchor(streamResource, "");
            tempLink.removeAll();
            tempLink.getElement().setAttribute("download", true);
            tempLink.getElement().setAttribute("hidden", true);
            tempLink.getStyle().setCursor("pointer");
            tempLink.getStyle().setColor("white");

            UI.getCurrent().getElement().appendChild(tempLink.getElement());
            tempLink.getElement().callJsFunction("click");

            close();
        });

        return generateButton;
    }

    private Set<String> mapPackageNames() {
        return project.getDomainModel().boundedContextMirrors().stream().map(
            BoundedContextMirror::getPackageName).collect(
            Collectors.toSet());
    }

    private String buildScriptFilename() {
        return project == null ? "dlc-project" + SQL_DDL_SCRIPT_SUFFIX : project.getName() + SQL_DDL_SCRIPT_SUFFIX;
    }

    private Button createCloseButton() {
        return new Button("Close", e -> close());
    }

    private InputStream getStream() {
        final String ddl = sqlDDLGeneratorService.generateSQL(
            project.getDomainModel(),
            generateDatabaseModelOptions.getBoundedContextPackage(),
            generateDatabaseModelOptions.getSelectedSqlDialect(),
            generateDatabaseModelOptions.isAuditModel());

        return new ByteArrayInputStream(ddl.getBytes(StandardCharsets.UTF_8));
    }

    @Data
    private static class GenerateDatabaseModelOptions {
        private String boundedContextPackage;
        private String selectedSqlDialect;
        private boolean auditModel;
    }
}
