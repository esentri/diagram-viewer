package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagram.domain.DomainDiagramGenerator;
import io.domainlifecycles.diagram.domain.config.DiagramTrimSettings;
import io.domainlifecycles.diagram.domain.config.DomainDiagramConfig;
import io.domainlifecycles.diagram.domain.config.GeneralVisualSettings;
import io.domainlifecycles.diagram.domain.config.LayoutSettings;
import io.domainlifecycles.diagram.domain.config.StyleSettings;
import io.domainlifecycles.diagram.domain.notes.DomainClassNote;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramTypeNote;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.Collections;
import java.util.List;

public class DiagrammerUtils {

    public static String generateNomnoml(
            DomainMirror domainMirror,
            DiagramStylingConfiguration diagramStylingConfiguration,
            DomainModelVisibility domainModelVisibility,
            List<DiagramTypeNote> notes
    ) {

        var classNotes = notes
                .stream()
                .map(n -> new DomainClassNote(n.getDomainTypeMirrorName(), n.getNotes()))
                .toList();

        DiagramTrimSettings trimSettings = DiagramTrimSettings.builder()
                .withExplicitlyIncludedPackageNames(domainModelVisibility.getExplicitlyIncludedPackagesNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getExplicitlyIncludedPackagesNames().stream().toList())
                .withIncludeConnectedTo(domainModelVisibility.getIncludeConnectedToClassNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getIncludeConnectedToClassNames().stream().toList())
                .withIncludeConnectedToIngoing(domainModelVisibility.getIncludeConnectedToIngoingClassNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getIncludeConnectedToIngoingClassNames().stream().toList())
                .withIncludeConnectedToOutgoing(domainModelVisibility.getIncludeConnectedToOutgoingClassNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getIncludeConnectedToOutgoingClassNames().stream().toList())
                .withExcludeConnectedToIngoing(domainModelVisibility.getExcludeConnectedToIngoingClassNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getExcludeConnectedToIngoingClassNames().stream().toList())
                .withExcludeConnectedToOutgoing(domainModelVisibility.getExcludeConnectedToOutgoingClassNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getExcludeConnectedToOutgoingClassNames().stream().toList())
                .withClassesBlacklist(domainModelVisibility.getBlacklistedClassNames() == null ?
                        Collections.emptyList() : domainModelVisibility.getBlacklistedClassNames().stream().toList())
                .build();

        StyleSettings styleSettings = StyleSettings.builder()
                .withAggregateFrameStyle(diagramStylingConfiguration.getAggregateFrameStyle())
                .withAggregateRootStyle(diagramStylingConfiguration.getAggregateRootStyle())
                .withEntityStyle(diagramStylingConfiguration.getEntityStyle())
                .withApplicationServiceStyle(diagramStylingConfiguration.getApplicationServiceStyle())
                .withDomainCommandStyle(diagramStylingConfiguration.getDomainCommandStyle())
                .withDomainEventStyle(diagramStylingConfiguration.getDomainEventStyle())
                .withDomainServiceStyle(diagramStylingConfiguration.getDomainServiceStyle())
                .withIdentityStyle(diagramStylingConfiguration.getIdentityStyle())
                .withOutboundServiceStyle(diagramStylingConfiguration.getOutboundServiceStyle())
                .withBackgroundColor(diagramStylingConfiguration.getBackgroundColor())
                .withFont(diagramStylingConfiguration.getFont().getNomnomlValue())
                .withQueryHandlerStyle(diagramStylingConfiguration.getQueryHandlerStyle())
                .withReadModelStyle(diagramStylingConfiguration.getReadModelStyle())
                .withRepositoryStyle(diagramStylingConfiguration.getRepositoryStyle())
                .withEnumStyle(diagramStylingConfiguration.getEnumStyle())
                .withUnspecifiedServiceKindStyle(diagramStylingConfiguration.getUnspecifiedServiceKindStyle())
                .withValueObjectStyle(diagramStylingConfiguration.getValueObjectStyle())
                .build();

        LayoutSettings layoutSettings = LayoutSettings.builder()
                .withAcycler(diagramStylingConfiguration.getAcycler().getNomnomlValue())
                .withDirection(diagramStylingConfiguration.getDirection().getNomnomlValue())
                .withRanker(diagramStylingConfiguration.getRanker().getNomnomlValue())
                .build();

        GeneralVisualSettings visualSettings = GeneralVisualSettings.builder()
                .withShowNotes(true)
                .withShowFields(diagramStylingConfiguration.isShowFields())
                .withShowMethods(diagramStylingConfiguration.isShowMethods())
                .withFieldStereotypes(diagramStylingConfiguration.isFieldStereotypes())
                .withFieldBlacklist(diagramStylingConfiguration.getFieldBlacklist())
                .withMethodBlacklist(diagramStylingConfiguration.getMethodBlacklist())
                .withShowAggregates(diagramStylingConfiguration.isShowAggregates())
                .withShowAggregateFields(diagramStylingConfiguration.isShowAggregateFields())
                .withShowAggregateMethods(diagramStylingConfiguration.isShowAggregateMethods())
                .withMultiplicityInLabel(diagramStylingConfiguration.isMultiplicityInLabel())
                .withCallApplicationServiceDriver(false)
                .withShowAssertions(diagramStylingConfiguration.isShowAssertions())
                .withShowApplicationServices(diagramStylingConfiguration.isShowApplicationServices())
                .withShowApplicationServiceFields(diagramStylingConfiguration.isShowApplicationServiceFields())
                .withShowApplicationServiceMethods(diagramStylingConfiguration.isShowApplicationServiceMethods())
                .withShowDomainCommands(diagramStylingConfiguration.isShowDomainCommands())
                .withShowDomainCommandFields(diagramStylingConfiguration.isShowDomainCommandFields())
                .withShowDomainCommandMethods(diagramStylingConfiguration.isShowDomainCommandMethods())
                .withShowDomainEvents(diagramStylingConfiguration.isShowDomainEvents())
                .withShowDomainEventFields(diagramStylingConfiguration.isShowDomainEventFields())
                .withShowDomainEventMethods(diagramStylingConfiguration.isShowDomainEventMethods())
                .withShowDomainServices(diagramStylingConfiguration.isShowDomainServices())
                .withShowDomainServiceFields(diagramStylingConfiguration.isShowDomainServiceFields())
                .withShowDomainServiceMethods(diagramStylingConfiguration.isShowDomainServiceMethods())
                .withShowFullQualifiedClassNames(diagramStylingConfiguration.isShowFullQualifiedClassNames())
                .withShowOnlyPublicMethods(diagramStylingConfiguration.isShowOnlyPublicMethods())
                .withShowObjectMembersInClasses(diagramStylingConfiguration.isShowObjectMembersInClasses())
                .withShowOnlyTopLevelDomainCommandRelations(diagramStylingConfiguration.isShowOnlyTopLevelDomainCommandRelations())
                .withShowInheritedMembersInClasses(diagramStylingConfiguration.isShowInheritedMembersInClasses())
                .withShowOutboundServices(diagramStylingConfiguration.isShowOutboundServices())
                .withShowOutboundServiceFields(diagramStylingConfiguration.isShowOutboundServiceFields())
                .withShowOutboundServiceMethods(diagramStylingConfiguration.isShowOutboundServiceMethods())
                .withShowQueryHandlers(diagramStylingConfiguration.isShowQueryHandlers())
                .withShowQueryHandlerFields(diagramStylingConfiguration.isShowQueryHandlerFields())
                .withShowQueryHandlerMethods(diagramStylingConfiguration.isShowQueryHandlerMethods())
                .withShowRepositories(diagramStylingConfiguration.isShowRepositories())
                .withShowRepositoryFields(diagramStylingConfiguration.isShowRepositoryFields())
                .withShowRepositoryMethods(diagramStylingConfiguration.isShowRepositoryMethods())
                .withShowReadModels(diagramStylingConfiguration.isShowReadModels())
                .withShowReadModelFields(diagramStylingConfiguration.isShowReadModelFields())
                .withShowReadModelMethods(diagramStylingConfiguration.isShowReadModelMethods())
                .withShowUnspecifiedServiceKinds(diagramStylingConfiguration.isShowUnspecifiedServiceKinds())
                .withShowUnspecifiedServiceKindFields(diagramStylingConfiguration.isShowUnspecifiedServiceKindFields())
                .withShowUnspecifiedServiceKindMethods(diagramStylingConfiguration.isShowUnspecifiedServiceKindMethods())
                .withShowAllInheritanceStructures(diagramStylingConfiguration.isShowAllInheritanceStructures())
                .withShowInheritanceStructuresForDomainCommands(diagramStylingConfiguration.isShowInheritanceStructuresForDomainCommands())
                .withShowInheritanceStructuresForDomainEvents(diagramStylingConfiguration.isShowInheritanceStructuresForDomainEvents())
                .withShowInheritanceStructuresForReadModels(diagramStylingConfiguration.isShowInheritanceStructuresForReadModels())
                .withShowInheritanceStructuresForServiceKinds(diagramStylingConfiguration.isShowInheritanceStructuresForServiceKinds())
                .withShowInheritanceStructuresInAggregates(diagramStylingConfiguration.isShowInheritanceStructuresInAggregates())
                .build();

        DomainDiagramConfig diagramConfig = DomainDiagramConfig.builder()
                .withGeneralVisualSettings(visualSettings)
                .withStyleSettings(styleSettings)
                .withLayoutSettings(layoutSettings)
                .withDiagramTrimSettings(trimSettings)
                .build();

        DomainDiagramGenerator generator = new DomainDiagramGenerator(
                diagramConfig,
                domainMirror,
                classNotes
        );
        return generator.generateDiagramText();
    }
}
