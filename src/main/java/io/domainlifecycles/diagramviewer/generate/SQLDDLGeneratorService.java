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


    public String generateSQL(DomainModel domainModel, String bcPackageName, String sqlDialect) {
        log.info("Generating ddl for {}", bcPackageName);
        SQLPrinter printer = null;
        var boundedContextName = bcPackageName.replaceAll("\\.", "\\_");
        var packageDescripton = new BoundedContextPackage(bcPackageName, boundedContextName);
        var dm = MirrorMapper.mapDomain(domainModel, packageDescripton);
        TargetConfig.TargetType targetType;
        switch (sqlDialect) {
            case "Oracle": {
                targetType = TargetConfig.TargetType.SQL_ORACLE;
                var target = SQLTargetConfig.builder()
                        .targetType(targetType)
                        .destinationPath("dummy")
                        .build();
                printer = new OracleSQLPrinter(target, dm);
            }

            default:{
                targetType = TargetConfig.TargetType.SQL_POSTGRES;
                var target = SQLTargetConfig.builder()
                        .targetType(targetType)
                        .destinationPath("dummy")
                        .build();
                printer = new PostgresSQLPrinter(target, dm);
            }

        };

        var bcDef = dm.findBoundedContextByName(boundedContextName);
        var source = printer.sourceCodeFile(bcDef);

        log.debug("Generated DDL:\n {}", source.content());
        log.info("Generated DDL for {} succesfully!", bcPackageName);
        return source.content();
    }

}
