package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import io.domainlifecycles.mirror.serialize.jackson2.JacksonDomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.jackson2.JacksonDomainCallsSerializer;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the mapping of the viewer's diagram configuration onto the DLC domain diagrammer for the
 * non-domain class and the backward flow settings, directly on the generated nomnoml text - no
 * database or Kroki needed.
 */
class DiagrammerUtilsTest {

    private static final String NON_DOMAIN_FIXTURE_PACKAGE = "fixtures.nondomain";

    private static DomainMirror nonDomainMirror;
    private static DomainMirror rezeptionMirror;
    private static DomainCalls rezeptionCalls;

    @BeforeAll
    static void initMirrors() {
        nonDomainMirror = new ReflectiveDomainMirrorFactory(NON_DOMAIN_FIXTURE_PACKAGE).initializeDomainMirror();
        rezeptionMirror = new JacksonDomainSerializer(false).deserialize(RezeptionScenario.domainMirrorJson());
        rezeptionCalls = new JacksonDomainCallsSerializer(false)
            .deserialize(RezeptionScenario.domainCallsJson(), rezeptionMirror);
    }

    @Test
    void Should_RenderNonDomainClassesRelatedToAServiceKind_When_DefaultConfigurationIsUsed() {

        // when
        String nomnoml = generate(nonDomainMirror, DiagramStylingConfiguration.builder().build(), new DomainModelVisibility(), null);

        // then: the mapper used by the service and the controller calling it are drawn, the
        // unrelated helper is not
        assertThat(nomnoml).contains(classBoxMarker("BookingMapper"));
        assertThat(nomnoml).contains(classBoxMarker("BookingController"));
        assertThat(nomnoml).doesNotContain(classBoxMarker("UnrelatedHelper"));
        assertThat(nomnoml).contains("[<AS>BookingApplicationService <<ApplicationService>>]  -> [<ND>BookingMapper <<NonDomain>>]");
        assertThat(nomnoml).contains("[<ND>BookingController <<NonDomain>>]  -> [<AS>BookingApplicationService <<ApplicationService>>]");
    }

    @Test
    void Should_HideNonDomainClasses_When_DisabledInTheConfiguration() {

        // given
        DiagramStylingConfiguration styling = DiagramStylingConfiguration.builder()
            .showNonDomainClasses(false)
            .build();

        // when
        String nomnoml = generate(nonDomainMirror, styling, new DomainModelVisibility(), null);

        // then
        assertThat(nomnoml).contains(classBoxMarker("BookingApplicationService"));
        assertThat(nomnoml).doesNotContain("<<NonDomain>>");
    }

    @Test
    void Should_ApplyTheConfiguredNonDomainClassStyle() {

        // given
        DiagramStylingConfiguration styling = DiagramStylingConfiguration.builder()
            .nonDomainClassStyle("fill=#123456 italic")
            .build();

        // when
        String nomnoml = generate(nonDomainMirror, styling, new DomainModelVisibility(), null);

        // then
        assertThat(nomnoml).contains("#.ND:fill=#123456 italic");
    }

    @Test
    void Should_OfferOnlyNonDomainClassesADiagramCanShow() {

        // when
        List<String> typeNames = DomainModelUtils
            .withoutUnrelatedNonDomainTypes(nonDomainMirror.getAllDomainTypeMirrors(), nonDomainMirror)
            .stream()
            .map(DomainTypeMirror::getTypeName)
            .toList();

        // then
        assertThat(typeNames).contains(
            NON_DOMAIN_FIXTURE_PACKAGE + ".BookingApplicationService",
            NON_DOMAIN_FIXTURE_PACKAGE + ".BookingMapper",
            NON_DOMAIN_FIXTURE_PACKAGE + ".BookingController");
        assertThat(typeNames).doesNotContain(NON_DOMAIN_FIXTURE_PACKAGE + ".UnrelatedHelper");
    }

    @Test
    void Should_RestrictDiagramToTheEntryChannels_When_BackwardFlowConfiguredForTheCheckOutEvent() {

        // given
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsTo(Set.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT));

        // when
        String nomnoml = generate(rezeptionMirror, DiagramStylingConfiguration.builder().build(), visibility, rezeptionCalls);

        // then: what leads into publishing the event is kept - its publisher, the service calling
        // it and the command processed on the way - while the unrelated check-in command and the
        // event's own listener (downstream, not leading into it) are not
        assertThat(nomnoml).contains(classBoxMarker("GastAusgecheckt"));
        assertThat(nomnoml).contains(classBoxMarker("Buchung"));
        assertThat(nomnoml).contains(classBoxMarker("BuchungApplicationService"));
        assertThat(nomnoml).contains(classBoxMarker("CheckeGastAus"));
        assertThat(nomnoml).doesNotContain(classBoxMarker("CheckeGastEin"));
        assertThat(nomnoml).doesNotContain(classBoxMarker("ZimmerFreigabeListener"));
    }

    @Test
    void Should_UniteForwardAndBackwardFlows_When_BothAreConfigured() {

        // given: backward into the check-out event, forward from the check-in command
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsTo(Set.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT))
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_IN_COMMAND));

        // when
        String nomnoml = generate(rezeptionMirror, DiagramStylingConfiguration.builder().build(), visibility, rezeptionCalls);

        // then
        assertThat(nomnoml).contains(classBoxMarker("CheckeGastAus"));
        assertThat(nomnoml).contains(classBoxMarker("CheckeGastEin"));
        assertThat(nomnoml).doesNotContain(classBoxMarker("AktualisiereGastdaten"));
    }

    @Test
    void Should_IgnoreConfiguredFlows_When_NoStaticAnalysisResultIsAvailable() {

        // given: forward and backward flows configured, but no DomainCalls at hand
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_COMMAND))
            .replaceIncludeFlowsTo(Set.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT));

        // when: rendering does not fail ...
        String nomnoml = generate(rezeptionMirror, DiagramStylingConfiguration.builder().build(), visibility, null);

        // then: ... and the diagram is not restricted to any flow
        assertThat(nomnoml).contains(classBoxMarker("CheckeGastEin"));
        assertThat(nomnoml).contains(classBoxMarker("AktualisiereGastdaten"));
    }

    private static String generate(DomainMirror mirror,
                                   DiagramStylingConfiguration styling,
                                   DomainModelVisibility visibility,
                                   DomainCalls domainCalls) {
        return DiagrammerUtils.generateNomnoml(mirror, styling, visibility, List.of(), domainCalls);
    }

    /**
     * The nomnoml declaration of a type rendered as its own class box, as opposed to merely being
     * named in a relationship or another type's member signature.
     */
    private static String classBoxMarker(String shortTypeName) {
        return "> " + shortTypeName + " <<";
    }
}
