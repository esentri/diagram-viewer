package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagram.domain.DomainDiagramGenerator;
import io.domainlifecycles.diagram.domain.config.DomainDiagramConfig;
import io.domainlifecycles.diagramviewer.model.DomainModelVisibility;
import io.domainlifecycles.mirror.api.DomainModel;

public class DiagrammerUtils {

    public static String generateNomnoml(
        DomainModel domainModel,
        String packageName,
        DomainModelVisibility domainModelVisibility) {

        DomainDiagramConfig diagramConfig = DomainDiagramConfig.builder()
                .withContextPackageName(packageName)
                .withTransitiveFilterSeedDomainServiceTypeNames(domainModelVisibility.getSeedClassNames())
                .withClassesBlacklist(domainModelVisibility.getBlacklistedClassNames())
                .build();
        DomainDiagramGenerator generator = new DomainDiagramGenerator(diagramConfig, domainModel);
        return generator.generateDiagramText();
    }
}
