package io.domainlifecycles.diagramviewer.sql;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.kickstart.configuration.target.SQLTargetConfig;
import io.domainlifecycles.kickstart.configuration.target.base.TargetConfig.TargetType;
import io.domainlifecycles.kickstart.map.BoundedContextPackage;
import io.domainlifecycles.kickstart.map.MirrorMapper;
import io.domainlifecycles.kickstart.model.GenDomainModel;
import io.domainlifecycles.kickstart.output.target.sql.OracleSQLPrinter;
import io.domainlifecycles.kickstart.output.target.sql.PostgresSQLPrinter;
import io.domainlifecycles.kickstart.output.target.sql.SQLPrinter;
import io.domainlifecycles.mirror.api.DomainMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class SQLDDLGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLDDLGeneratorService.class);

    public String generateSQL(DomainMirror domainMirror, String boundedContextPackageName, String sqlDialect, boolean audit) {
        LOGGER.info(String.format("Generating DDL for '%s'...", boundedContextPackageName));

        BoundedContextPackage packageDescription = new BoundedContextPackage(boundedContextPackageName, boundedContextPackageName);
        GenDomainModel dm = MirrorMapper.mapDomain(domainMirror, packageDescription);

        SQLPrinter printer = getPrinterImplementation(boundedContextPackageName, sqlDialect, audit, dm);

        LOGGER.info("Generated DDL for '{}' successfully!", boundedContextPackageName);
        return printer.sourceCodeFile(dm.findBoundedContextByName(boundedContextPackageName)).content();
    }

    private SQLPrinter getPrinterImplementation(String bcPackageName, String sqlDialect, boolean audit, GenDomainModel dm) {
        SQLPrinter printer;
        switch (sqlDialect) {
            case "Oracle" -> {
                var target = SQLTargetConfig.builder()
                    .targetType(TargetType.SQL_ORACLE)
                    .destinationPath("dummy")
                    .auditSchema(audit ? bcPackageName : null)
                    .generateAuditTables(audit)
                    .build();
                printer = new OracleSQLPrinter(target, dm);
            }
            case "Postgres" -> {
                var target = SQLTargetConfig.builder()
                    .targetType(TargetType.SQL_POSTGRES)
                    .destinationPath("dummy")
                    .auditSchema(audit ? bcPackageName : null)
                    .generateAuditTables(audit)
                    .build();
                printer = new PostgresSQLPrinter(target, dm);
            }
            default -> throw DiagramViewerException.fail(String.format("'%s' is not a valid SQL Dialect.", sqlDialect));
        }
        return printer;
    }
}
