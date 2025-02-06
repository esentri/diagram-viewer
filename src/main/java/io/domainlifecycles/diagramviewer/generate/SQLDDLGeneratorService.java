package io.domainlifecycles.diagramviewer.generate;

import io.domainlifecycles.kickstart.configuration.target.SQLTargetConfig;
import io.domainlifecycles.kickstart.configuration.target.base.TargetConfig;
import io.domainlifecycles.kickstart.map.BoundedContextPackage;
import io.domainlifecycles.kickstart.map.MirrorMapper;
import io.domainlifecycles.kickstart.output.target.sql.OracleSQLPrinter;
import io.domainlifecycles.kickstart.output.target.sql.PostgresSQLPrinter;
import io.domainlifecycles.kickstart.output.target.sql.SQLPrinter;
import io.domainlifecycles.mirror.api.DomainModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class SQLDDLGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(SQLDDLGeneratorService.class);


    public SQLDDLGeneratorService() {

    }


    public String generateSQL(DomainModel domainModel, String bcPackageName, String bcSchemaName, String sqlDialect, boolean audit) {
        log.info("Generating ddl for {}", bcPackageName);
        SQLPrinter printer = null;
        var packageDescripton = new BoundedContextPackage(bcPackageName, bcSchemaName);
        var dm = MirrorMapper.mapDomain(domainModel, packageDescripton);
        TargetConfig.TargetType targetType;
        String auditSchema = null;
        if (audit) {
            auditSchema = bcSchemaName;
        }
        switch (sqlDialect) {
            case "Oracle": {
                targetType = TargetConfig.TargetType.SQL_ORACLE;
                var target = SQLTargetConfig.builder()
                        .targetType(targetType)
                        .destinationPath("dummy")
                        .auditSchema(auditSchema)
                        .generateAuditTables(audit)
                        .build();
                printer = new OracleSQLPrinter(target, dm);
                break;
            }

            default:{
                targetType = TargetConfig.TargetType.SQL_POSTGRES;
                var target = SQLTargetConfig.builder()
                        .targetType(targetType)
                        .destinationPath("dummy")
                        .auditSchema(auditSchema)
                        .generateAuditTables(audit)
                        .build();
                printer = new PostgresSQLPrinter(target, dm);
            }

        };

        var bcDef = dm.findBoundedContextByName(bcSchemaName);
        var source = printer.sourceCodeFile(bcDef);

        log.debug("Generated DDL:\n {}", source.content());
        log.info("Generated DDL for {} successfully!", bcPackageName);
        return source.content();
    }

}
