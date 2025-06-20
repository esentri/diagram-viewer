package io.domainlifecycles.diagramviewer.sql;

import io.domainlifecycles.diagramviewer.plugin.SQLDDLGeneratorService;
import io.domainlifecycles.diagramviewer.plugin.SQLDialect;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NoOpSQLDDLGeneratorService implements SQLDDLGeneratorService {

    @Override
    public String generateSQL(DomainMirror domainMirror, AggregateRootMirror aggregateRootMirror, SQLDialect sqlDialect, boolean audit, String sqlSchemaName) {
        log.error("This service should never be called!");
        return "";
    }
}
