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
 *  Copyright 2025-2026 the original author or authors.
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
import com.vaadin.flow.server.streams.DownloadHandler;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.plugin.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.plugin.SQLDialect;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
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
        generateButton.getStyle().setCursor("pointer");

        Anchor downloadSqlScriptAnchor = new Anchor((DownloadHandler) download -> {
            binder.writeBeanIfValid(generateDatabaseModelOptions);
            download.setFileName(buildScriptFilename());
            download.setContentType(APPLICATION_SQL_MIME_TYPE);
            download.getOutputStream().write(getSqlScriptFileContents());
        }, "");

        downloadSqlScriptAnchor.removeAll();
        downloadSqlScriptAnchor.getElement().setAttribute("download", true);
        downloadSqlScriptAnchor.getElement().setAttribute("hidden", true);
        downloadSqlScriptAnchor.getStyle().setColor("white");

        generateButton.addClickListener(event -> {
            UI.getCurrent().getElement().appendChild(downloadSqlScriptAnchor.getElement());
            downloadSqlScriptAnchor.getElement().callJsFunction("click");

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

    private byte[] getSqlScriptFileContents() {
        final String ddl = sqlDDLGeneratorService.generateSQL(
            sessionStorage.getDomainMirror(project.getId()),
            generateDatabaseModelOptions.getSelectedAggregateRootMirror(),
            generateDatabaseModelOptions.getSelectedSqlDialect(),
            generateDatabaseModelOptions.isAuditModel(),
            generateDatabaseModelOptions.getSchemaName()
        );

        return ddl.getBytes(StandardCharsets.UTF_8);
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
