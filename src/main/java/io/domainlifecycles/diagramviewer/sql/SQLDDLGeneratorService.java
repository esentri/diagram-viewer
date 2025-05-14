package io.domainlifecycles.diagramviewer.sql;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.kickstart.configuration.target.SQLTargetConfig;
import io.domainlifecycles.kickstart.configuration.target.base.TargetConfig.TargetType;
import io.domainlifecycles.kickstart.map.BoundedContextPackage;
import io.domainlifecycles.kickstart.map.MirrorMapper;
import io.domainlifecycles.kickstart.model.AggregateRoot;
import io.domainlifecycles.kickstart.model.DomainBaseObject;
import io.domainlifecycles.kickstart.model.GenDomainModel;
import io.domainlifecycles.kickstart.output.target.sql.OracleSQLPrinter;
import io.domainlifecycles.kickstart.output.target.sql.PostgresSQLPrinter;
import io.domainlifecycles.kickstart.output.target.sql.SQLPrinter;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class SQLDDLGeneratorService {

    private final SessionStorage sessionStorage;

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLDDLGeneratorService.class);

    public SQLDDLGeneratorService(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public String generateSQL(
            UUID projectId,
            AggregateRootMirror aggregateRootMirror,
            String sqlDialect,
            boolean audit,
            String sqlSchemaName) {
        LOGGER.info(String.format("Generating DDL for '%s'...", aggregateRootMirror));
        DomainMirror domainMirror = sessionStorage.getDomainMirror(projectId);
        GenDomainModel dm;
        try {
            dm = MirrorMapper.mapDomain(domainMirror, new BoundedContextPackage("DEFAULT", sqlSchemaName));
        } catch(IllegalStateException e) {
            throw DiagramViewerException.fail("Could not map DomainModel for SQL-DDL generation.", e);
        }

        String aggregateRootFullQualifiedTypeName = aggregateRootMirror.getTypeName();
        String aggregateRootTypeName = aggregateRootFullQualifiedTypeName.contains(".")
            ? aggregateRootFullQualifiedTypeName.substring(aggregateRootFullQualifiedTypeName.lastIndexOf('.') + 1)
            : aggregateRootFullQualifiedTypeName;

        DomainBaseObject baseObject = dm.findDomainBaseObjectByName(null, aggregateRootTypeName);

        SQLPrinter printer = getPrinterImplementation(sqlDialect, audit, dm, sqlSchemaName);

        LOGGER.info("Generated DDL for '{}' successfully!", aggregateRootMirror);
        return printer.sourceCodeFile((AggregateRoot) baseObject).content();
    }

    private SQLPrinter getPrinterImplementation(
            String sqlDialect,
            boolean audit,
            GenDomainModel dm,
            String sqlSchemaName) {
        SQLPrinter printer;
        switch (sqlDialect) {
            case "Oracle" -> {
                var target = SQLTargetConfig.builder()
                    .targetType(TargetType.SQL_ORACLE)
                    .destinationPath("dummy")
                    .generateAuditTables(audit)
                    .auditSchema(sqlSchemaName)
                    .build();
                printer = new OracleSQLPrinter(target, dm);
            }
            case "Postgres" -> {
                var target = SQLTargetConfig.builder()
                    .targetType(TargetType.SQL_POSTGRES)
                    .destinationPath("dummy")
                    .generateAuditTables(audit)
                    .auditSchema(sqlSchemaName)
                    .build();
                printer = new PostgresSQLPrinter(target, dm);
            }
            default -> throw DiagramViewerException.fail(String.format("'%s' is not a valid SQL Dialect.", sqlDialect));
        }
        return printer;
    }
}
