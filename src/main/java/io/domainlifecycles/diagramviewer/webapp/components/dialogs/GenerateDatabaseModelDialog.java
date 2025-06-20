package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.UI;
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
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.plugin.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.plugin.SQLDialect;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.Data;

public class GenerateDatabaseModelDialog extends Dialog {
    private static final SQLDialect[] SQL_DIALECT_SELECT_VALUES = {SQLDialect.ORACLE, SQLDialect.POSTGRES};
    private static final String SQL_DDL_SCRIPT_SUFFIX = "-ddl-script.sql";
    private static final String APPLICATION_SQL_MIME_TYPE = "application/sql";

    private final Binder<GenerateDatabaseModelOptions> binder;
    private final SQLDDLGeneratorService sqlDDLGeneratorService;
    private final Project project;
    private final List<AggregateRootMirror> allAggregateRootMirrors;
    private final SessionStorage sessionStorage;

    private GenerateDatabaseModelOptions generateDatabaseModelOptions;
    private Button generateButton;

    public GenerateDatabaseModelDialog(
            SessionStorage sessionStorage,
            SQLDDLGeneratorService sqlDDLGeneratorService,
            Project project
    ) {
        this.sessionStorage = sessionStorage;
        this.sqlDDLGeneratorService = sqlDDLGeneratorService;
        this.project = project;
        this.allAggregateRootMirrors = sessionStorage.getAllAggregateRootMirrors(project.getId()).stream()
                .filter(m -> !m.getTypeName().startsWith("io.domainlifecycles"))
                .toList();
        this.binder = new Binder<>();

        setHeaderTitle("Download SQL-DDL-Model");
        getFooter().add(createGenerateButton());
        getFooter().add(createCloseButton());
        add(createDialogLayout());

        addOpenedChangeListener(e -> {
            if(e.isOpened()) {
                this.generateDatabaseModelOptions = new GenerateDatabaseModelOptions(SQL_DIALECT_SELECT_VALUES[0]);
                binder.readBean(generateDatabaseModelOptions);
            }
        });

        binder.addStatusChangeListener(event -> generateButton.setEnabled(binder.isValid()));
    }

    private FormLayout createDialogLayout() {
        FormLayout formLayout = new FormLayout();

        Select<SQLDialect> sqlDialectSelect = new Select<>();
        sqlDialectSelect.setItems(SQL_DIALECT_SELECT_VALUES);
        sqlDialectSelect.setValue(SQL_DIALECT_SELECT_VALUES[0]);
        binder.forField(sqlDialectSelect)
            .bind(GenerateDatabaseModelOptions::getSelectedSqlDialect, GenerateDatabaseModelOptions::setSelectedSqlDialect);
        formLayout.addFormItem(sqlDialectSelect, "SQL Dialect");

        Select<AggregateRootMirror> aggregateRootMirrorSelect = new Select<>();
        aggregateRootMirrorSelect.setItems(allAggregateRootMirrors);
        aggregateRootMirrorSelect.setItemLabelGenerator(DomainTypeMirror::getTypeName);
        binder.forField(aggregateRootMirrorSelect)
            .asRequired("Domain-Type is required.")
            .bind(GenerateDatabaseModelOptions::getSelectedAggregateRootMirror, GenerateDatabaseModelOptions::setSelectedAggregateRootMirror);
        formLayout.addFormItem(aggregateRootMirrorSelect, "Domain-Type");

        Checkbox auditModelCheckbox = new Checkbox();
        binder.forField(auditModelCheckbox).bind(GenerateDatabaseModelOptions::isAuditModel, GenerateDatabaseModelOptions::setAuditModel);
        formLayout.addFormItem(auditModelCheckbox, "Audit Model");

        TextField schemaNameTextField = new TextField();
        binder.forField(schemaNameTextField)
                .asRequired("Schema name is required.")
                .bind(GenerateDatabaseModelOptions::getSchemaName, GenerateDatabaseModelOptions::setSchemaName);
        formLayout.addFormItem(schemaNameTextField, "Schema Name");

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

    private String buildScriptFilename() {
        return project == null ? "dlc-project" + SQL_DDL_SCRIPT_SUFFIX : project.getName() + SQL_DDL_SCRIPT_SUFFIX;
    }

    private Button createCloseButton() {
        return new Button("Close", e -> close());
    }

    private InputStream getStream() {
        final String ddl = sqlDDLGeneratorService.generateSQL(
            sessionStorage.getDomainMirror(project.getId()),
            generateDatabaseModelOptions.getSelectedAggregateRootMirror(),
            generateDatabaseModelOptions.getSelectedSqlDialect(),
            generateDatabaseModelOptions.isAuditModel(),
            generateDatabaseModelOptions.getSchemaName()
        );

        return new ByteArrayInputStream(ddl.getBytes(StandardCharsets.UTF_8));
    }

    @Data
    private static class GenerateDatabaseModelOptions {
        private AggregateRootMirror selectedAggregateRootMirror;
        private SQLDialect selectedSqlDialect;
        private boolean auditModel;
        private String schemaName;

        public GenerateDatabaseModelOptions(SQLDialect selectedSqlDialect) {
            this.selectedSqlDialect = selectedSqlDialect;
        }
    }
}
