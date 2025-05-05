package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagram.domain.DomainDiagramGenerator;
import io.domainlifecycles.diagram.domain.config.DomainDiagramConfig;
import io.domainlifecycles.diagramviewer.model.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.DomainModelVisibility;
import io.domainlifecycles.mirror.api.DomainMirror;

public class DiagrammerUtils {

    public static String generateNomnoml(
            DomainMirror domainMirror,
            DiagramStylingConfiguration diagramStylingConfiguration,
            DomainModelVisibility domainModelVisibility) {

        DomainDiagramConfig diagramConfig = DomainDiagramConfig.builder()
                .withFilteredPackageNames(domainModelVisibility.getFilteredPackageNames().stream().toList())
            .withAggregateRootStyle(diagramStylingConfiguration.getAggregateRootStyle())
            .withAggregateFrameStyle(diagramStylingConfiguration.getAggregateFrameStyle())
            .withEntityStyle(diagramStylingConfiguration.getEntityStyle())
            .withValueObjectStyle(diagramStylingConfiguration.getValueObjectStyle())
            .withEnumStyle(diagramStylingConfiguration.getEnumStyle())
            .withIdentityStyle(diagramStylingConfiguration.getIdentityStyle())
            .withDomainEventStyle(diagramStylingConfiguration.getDomainEventStyle())
            .withDomainCommandStyle(diagramStylingConfiguration.getDomainCommandStyle())
            .withApplicationServiceStyle(diagramStylingConfiguration.getApplicationServiceStyle())
            .withDomainServiceStyle(diagramStylingConfiguration.getDomainServiceStyle())
            .withRepositoryStyle(diagramStylingConfiguration.getRepositoryStyle())
            .withReadModelStyle(diagramStylingConfiguration.getReadModelStyle())
            .withQueryHandlerStyle(diagramStylingConfiguration.getQueryHandlerStyle())
            .withOutboundServiceStyle(diagramStylingConfiguration.getOutboundServiceStyle())
            .withUnspecifiedServiceKindStyle(diagramStylingConfiguration.getUnspecifiedServiceKindStyle())
            .withFont(diagramStylingConfiguration.getFont().getNomnomlValue())
            .withDirection(diagramStylingConfiguration.getDirection().getNomnomlValue())
            .withRanker(diagramStylingConfiguration.getRanker().getNomnomlValue())
            .withAcycler(diagramStylingConfiguration.getAcycler().getNomnomlValue())
            .withBackgroundColor(diagramStylingConfiguration.getBackgroundColor())
            .withShowFields(diagramStylingConfiguration.isShowFields())
            .withShowFullQualifiedClassNames(diagramStylingConfiguration.isShowFullQualifiedClassNames())
            .withShowAssertions(diagramStylingConfiguration.isShowAssertions())
            .withShowMethods(diagramStylingConfiguration.isShowMethods())
            .withShowOnlyPublicMethods(diagramStylingConfiguration.isShowOnlyPublicMethods())
            .withShowDomainEvents(diagramStylingConfiguration.isShowDomainEvents())
            .withShowDomainEventFields(diagramStylingConfiguration.isShowDomainEventFields())
            .withShowDomainEventMethods(diagramStylingConfiguration.isShowDomainEventMethods())
            .withShowDomainCommands(diagramStylingConfiguration.isShowDomainCommands())
            .withShowOnlyTopLevelDomainCommandRelations(diagramStylingConfiguration.isShowOnlyTopLevelDomainCommandRelations())
            .withShowDomainCommandFields(diagramStylingConfiguration.isShowDomainCommandFields())
            .withShowDomainCommandMethods(diagramStylingConfiguration.isShowDomainCommandMethods())
            .withShowDomainServices(diagramStylingConfiguration.isShowDomainServices())
            .withShowDomainServiceFields(diagramStylingConfiguration.isShowDomainServiceFields())
            .withShowDomainServiceMethods(diagramStylingConfiguration.isShowDomainServiceMethods())
            .withShowApplicationServices(diagramStylingConfiguration.isShowApplicationServices())
            .withShowApplicationServiceFields(diagramStylingConfiguration.isShowApplicationServiceFields())
            .withShowApplicationServiceMethods(diagramStylingConfiguration.isShowApplicationServiceMethods())
            .withShowRepositories(diagramStylingConfiguration.isShowRepositories())
            .withShowRepositoryFields(diagramStylingConfiguration.isShowRepositoryFields())
            .withShowRepositoryMethods(diagramStylingConfiguration.isShowRepositoryMethods())
            .withShowReadModels(diagramStylingConfiguration.isShowReadModels())
            .withShowReadModelFields(diagramStylingConfiguration.isShowReadModelFields())
            .withShowReadModelMethods(diagramStylingConfiguration.isShowReadModelMethods())
            .withShowQueryHandlers(diagramStylingConfiguration.isShowQueryHandlers())
            .withShowQueryHandlerFields(diagramStylingConfiguration.isShowQueryHandlerFields())
            .withShowQueryHandlerMethods(diagramStylingConfiguration.isShowQueryHandlerMethods())
            .withShowOutboundServices(diagramStylingConfiguration.isShowOutboundServices())
            .withShowOutboundServiceFields(diagramStylingConfiguration.isShowOutboundServiceFields())
            .withShowOutboundServiceMethods(diagramStylingConfiguration.isShowOutboundServiceMethods())
            .withShowUnspecifiedServiceKinds(diagramStylingConfiguration.isShowUnspecifiedServiceKinds())
            .withShowUnspecifiedServiceKindFields(diagramStylingConfiguration.isShowUnspecifiedServiceKindFields())
            .withShowUnspecifiedServiceKindMethods(diagramStylingConfiguration.isShowUnspecifiedServiceKindMethods())
            .withCallApplicationServiceDriver(diagramStylingConfiguration.isCallApplicationServiceDriver())
            .withFieldBlacklist(diagramStylingConfiguration.getFieldBlacklist())
            .withMethodBlacklist(diagramStylingConfiguration.getMethodBlacklist())
            .withShowInheritedMembersInClasses(diagramStylingConfiguration.isShowInheritedMembersInClasses())
            .withShowObjectMembersInClasses(diagramStylingConfiguration.isShowObjectMembersInClasses())
            .withMultiplicityInLabel(diagramStylingConfiguration.isMultiplicityInLabel())
            .withFieldStereotypes(diagramStylingConfiguration.isFieldStereotypes())
            .withTransitiveFilterSeedDomainServiceTypeNames(domainModelVisibility.getSeedClassNames().stream().toList())
            .withClassesBlacklist(domainModelVisibility.getBlacklistedClassNames().stream().toList())
                .build();

        DomainDiagramGenerator generator = new DomainDiagramGenerator(diagramConfig, domainMirror);
        return generator.generateDiagramText();
    }
}
