package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagram.domain.DomainDiagramGenerator;
import io.domainlifecycles.diagram.domain.config.DomainDiagramConfig;

import java.util.List;

public class DiagrammerUtils {

    public static String generateNomnoml(String packageName){
        DomainDiagramConfig diagramConfig = DomainDiagramConfig.builder()
                .withContextPackageName(packageName)
                .withTransitiveFilterSeedDomainServiceTypeNames(List.of("com.esentri.rezeption.core.inport.BuchungUseCases"))
                .build();
        DomainDiagramGenerator generator = new DomainDiagramGenerator(diagramConfig);
        return generator.generateDiagramText();
    }
}
