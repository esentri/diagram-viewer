package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagram.domain.DomainDiagramGenerator;
import io.domainlifecycles.diagram.domain.config.DomainDiagramConfig;
import io.domainlifecycles.mirror.api.DomainModel;
import java.util.List;

public class DiagrammerUtils {

    public static String generateNomnoml(DomainModel domainModel, String packageName, List<String> seedClassNames){
        DomainDiagramConfig diagramConfig = DomainDiagramConfig.builder()
                .withContextPackageName(packageName)
                .withTransitiveFilterSeedDomainServiceTypeNames(seedClassNames)
                .build();
        DomainDiagramGenerator generator = new DomainDiagramGenerator(diagramConfig, domainModel);
        return generator.generateDiagramText();
    }
}
