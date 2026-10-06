/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
import io.domainlifecycles.mirror.api.ValueMirror;
import io.domainlifecycles.staticanalysis.DomainCalls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiagrammerUtils {

    private static final String AGGREGATE_FRAME_PREFIX = "[<AF";

    public static String generateNomnoml(
            DomainMirror domainMirror,
            DiagramStylingConfiguration diagramStylingConfiguration,
            DomainModelVisibility domainModelVisibility,
            List<DiagramTypeNote> notes,
            DomainCalls domainCalls
    ) {

        var classNotes = notes
                .stream()
                .map(n -> new DomainClassNote(n.getDomainTypeMirrorName(), n.getNotes()))
                .toList();

        var blackListedClasses = new ArrayList<String>();
        if(domainModelVisibility.getBlacklistedClassNames() != null){
            blackListedClasses.addAll(domainModelVisibility.getBlacklistedClassNames());
        }
        if(domainModelVisibility.getInlinedValueObjects() != null){
            blackListedClasses.addAll(domainModelVisibility.getInlinedValueObjects());
        }

        DiagramTrimSettings trimSettings = DiagramTrimSettings.builder()
                .withExplicitlyIncludedPackageNames(domainModelVisibility.getEffectiveIncludedPackages().stream().toList())
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
                .withClassesBlacklist(blackListedClasses)
                // flow filtering needs the result of a static analysis: without one, configured flows
                // are ignored (but kept, so they apply again once an analysis result is uploaded)
                .withIncludeFlowsFrom(domainCalls == null || domainModelVisibility.getIncludeFlowsFrom() == null ?
                        Collections.emptyList() : domainModelVisibility.getIncludeFlowsFrom().stream().toList())
                .withIncludeFlowsTo(domainCalls == null || domainModelVisibility.getIncludeFlowsTo() == null ?
                        Collections.emptyList() : domainModelVisibility.getIncludeFlowsTo().stream().toList())
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
                .withFactoryStyle(diagramStylingConfiguration.getFactoryStyle())
                .withBackgroundColor(diagramStylingConfiguration.getBackgroundColor())
                .withFont(diagramStylingConfiguration.getFont().getNomnomlValue())
                .withQueryHandlerStyle(diagramStylingConfiguration.getQueryHandlerStyle())
                .withReadModelStyle(diagramStylingConfiguration.getReadModelStyle())
                .withRepositoryStyle(diagramStylingConfiguration.getRepositoryStyle())
                .withEnumStyle(diagramStylingConfiguration.getEnumStyle())
                .withUnspecifiedServiceKindStyle(diagramStylingConfiguration.getUnspecifiedServiceKindStyle())
                .withNonDomainClassStyle(diagramStylingConfiguration.getNonDomainClassStyle())
                .withValueObjectStyle(diagramStylingConfiguration.getValueObjectStyle())
                .build();

        LayoutSettings layoutSettings = LayoutSettings.builder()
                .withAcycler(diagramStylingConfiguration.getAcycler().getNomnomlValue())
                .withDirection(diagramStylingConfiguration.getDirection().getNomnomlValue())
                .withRanker(diagramStylingConfiguration.getRanker().getNomnomlValue())
                .build();

        GeneralVisualSettings visualSettings = GeneralVisualSettings.builder()
                .withShowNotes(true)
                .withShowRelationshipLabels(diagramStylingConfiguration.isShowRelationLabels())
                .withShowRelationshipStereotypes(diagramStylingConfiguration.isShowRelationStereotypes())
                .withShowFields(diagramStylingConfiguration.isShowFields())
                .withShowMethods(diagramStylingConfiguration.isShowMethods())
                .withFieldStereotypes(diagramStylingConfiguration.isFieldStereotypes())
                .withFieldBlacklist(diagramStylingConfiguration.getFieldBlacklist())
                .withMethodBlacklist(diagramStylingConfiguration.getMethodBlacklist())
                .withShowAggregates(diagramStylingConfiguration.isShowAggregates())
                .withShowAggregateFields(diagramStylingConfiguration.isShowAggregateFields())
                .withShowAggregateMethods(diagramStylingConfiguration.isShowAggregateMethods())
                .withShowOnlyAggregateFrames(diagramStylingConfiguration.isShowOnlyAggregateFrames())
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
                .withShowFactories(diagramStylingConfiguration.isShowFactories())
                .withShowFactoryFields(diagramStylingConfiguration.isShowFactoryFields())
                .withShowFactoryMethods(diagramStylingConfiguration.isShowFactoryMethods())
                .withShowFactoryRelations(diagramStylingConfiguration.isShowFactoryRelations())
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
                .withShowNonDomainClasses(diagramStylingConfiguration.isShowNonDomainClasses())
                .withShowNonDomainClassFields(diagramStylingConfiguration.isShowNonDomainClassFields())
                .withShowNonDomainClassMethods(diagramStylingConfiguration.isShowNonDomainClassMethods())
                .withMaxInlinedValueObjectFields(diagramStylingConfiguration.getMaxInlinedValueObjectFields())
                .withShowOnlyFlowMethods(diagramStylingConfiguration.isShowOnlyFlowMethods())
                // without an analysis there are no flows whose calls could be drawn
                .withShowFlowCallRelations(domainCalls != null && diagramStylingConfiguration.isShowFlowCallRelations())
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
                classNotes,
                domainCalls
        );
        return generator.generateDiagramText();
    }

    /**
     * Counts the classes of a generated nomnoml diagram: its class boxes, without the frames enclosing aggregates. A
     * frame drawn without its content - an aggregate shown as frame only - counts as a class box itself.
     *
     * @param nomnoml the diagram text as generated by {@link #generateNomnoml}
     * @return the number of classes
     */
    public static int countClasses(String nomnoml) {
        return (int) nomnoml.lines()
            .map(String::stripLeading)
            .filter(line -> line.startsWith("[<") && !(line.startsWith(AGGREGATE_FRAME_PREFIX) && line.endsWith("|")))
            .count();
    }
}
