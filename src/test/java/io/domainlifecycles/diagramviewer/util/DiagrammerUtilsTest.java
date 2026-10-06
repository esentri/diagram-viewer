package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.DomainMethod;
import io.domainlifecycles.staticanalysis.serialize.jackson3.JacksonDomainCallsSerializer;
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

    private static final String ZIMMER_APPLICATION_SERVICE = "com.esentri.rezeption.application.zimmer.ZimmerApplicationService";

    private static final String NON_DOMAIN_FIXTURE_PACKAGE = "fixtures.nondomain";

    private static final String FACTORY_FIXTURE_PACKAGE = "fixtures.factory";

    private static final String CREATES_ORDER =
        "[<F>OrderFactory <<Factory>>]  --[<label> <<creates>> OrderFactory.create] --> [<AF> Order <<Aggregate>>]";

    private static DomainMirror nonDomainMirror;
    private static DomainMirror factoryMirror;
    private static DomainMirror connectionDepthMirror;
    private static DomainMirror rezeptionMirror;
    private static DomainCalls rezeptionCalls;

    @BeforeAll
    static void initMirrors() {
        nonDomainMirror = new ReflectiveDomainMirrorFactory(NON_DOMAIN_FIXTURE_PACKAGE).initializeDomainMirror();
        factoryMirror = new ReflectiveDomainMirrorFactory(FACTORY_FIXTURE_PACKAGE).initializeDomainMirror();
        connectionDepthMirror = new ReflectiveDomainMirrorFactory("fixtures.connectiondepth").initializeDomainMirror();
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
    void Should_RenderAFactoryAndWhatItCreates_When_DefaultConfigurationIsUsed() {

        // when
        String nomnoml = generate(factoryMirror, DiagramStylingConfiguration.builder().build(), new DomainModelVisibility(), null);

        // then
        assertThat(nomnoml).contains("[<F> OrderFactory <<Factory>> |");
        assertThat(nomnoml).contains("#.F:fill=#E0F0E0 bold");
        assertThat(nomnoml).contains(CREATES_ORDER);
        assertThat(nomnoml).contains("+ «factory» Order reorder(Order)");
    }

    @Test
    void Should_HideFactoriesAndTheirCreatesRelations_When_DisabledInTheConfiguration() {

        // when
        String withoutFactories = generate(factoryMirror,
            DiagramStylingConfiguration.builder().showFactories(false).build(), new DomainModelVisibility(), null);
        String withoutRelations = generate(factoryMirror,
            DiagramStylingConfiguration.builder().showFactoryRelations(false).build(), new DomainModelVisibility(), null);

        // then
        assertThat(withoutFactories).doesNotContain("OrderFactory");
        assertThat(withoutRelations).contains(classBoxMarker("OrderFactory")).doesNotContain("<<creates>>");
    }

    @Test
    void Should_DrawOnlyTheFramesOfTheAggregates_When_EnabledInTheConfiguration() {

        // when
        String withContent = generate(factoryMirror, DiagramStylingConfiguration.builder().build(),
            new DomainModelVisibility(), null);
        String framesOnly = generate(factoryMirror, DiagramStylingConfiguration.builder().showOnlyAggregateFrames(true).build(),
            new DomainModelVisibility(), null);

        // then: the frame stays connected, the aggregate root inside is gone
        assertThat(withContent).contains("[<AF> Order <<Aggregate>>|").contains("[<AR> Order <<AggregateRoot>>");
        assertThat(framesOnly.lines()).contains("[<AF> Order <<Aggregate>>]");
        assertThat(framesOnly).doesNotContain("<<AggregateRoot>>").contains(CREATES_ORDER);
    }

    @Test
    void Should_FollowTheConnectionsUpToTheDepthOfTheDiagram() {

        // given: EntryApplicationService -> MiddleDomainService -> LastDomainService
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeConnectedToOutgoingClassNames(Set.of("fixtures.connectiondepth.EntryApplicationService"));

        // when
        String completePath = generate(connectionDepthMirror, DiagramStylingConfiguration.builder().build(),
            visibility, null);
        String oneStep = generate(connectionDepthMirror, DiagramStylingConfiguration.builder().build(),
            visibility.replaceIncludeConnectedDepths(0, 1), null);

        // then
        assertThat(completePath).contains(classBoxMarker("MiddleDomainService"), classBoxMarker("LastDomainService"));
        assertThat(oneStep).contains(classBoxMarker("MiddleDomainService"))
            .doesNotContain(classBoxMarker("LastDomainService"));
    }

    @Test
    void Should_ApplyTheFactoryStyleAndMemberSettings() {

        // when
        String nomnoml = generate(factoryMirror, DiagramStylingConfiguration.builder()
            .factoryStyle("fill=#123456 bold")
            .showFactoryMethods(false)
            .build(), new DomainModelVisibility(), null);

        // then
        assertThat(nomnoml).contains("#.F:fill=#123456 bold");
        assertThat(nomnoml).doesNotContain("Order create(OrderId)");
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

    @Test
    void Should_RenderWithoutCallRelations_When_SwitchedOnButNoStaticAnalysisResultIsAvailable() {

        // given: the calls of the flows switched on, but no DomainCalls at hand
        DiagramStylingConfiguration styling = DiagramStylingConfiguration.builder().showFlowCallRelations(true).build();
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_COMMAND));

        // when / then: the stored switch does not break rendering
        String nomnoml = generate(rezeptionMirror, styling, visibility, null);
        assertThat(nomnoml).doesNotContain("<<calls>>");
    }

    @Test
    void Should_ShowValueObjectsOfTwoFieldsInline_ByDefault_And_AsClass_When_TheMaximumIsLowered() {

        // given: Zeitraum has two fields, start and ende
        String zeitraumBox = "// !!! com.esentri.rezeption.domain.Zeitraum !!!";

        // when
        String byDefault = generate(rezeptionMirror, DiagramStylingConfiguration.builder().build(),
            new DomainModelVisibility(), rezeptionCalls);
        String lowered = generate(rezeptionMirror, DiagramStylingConfiguration.builder().maxInlinedValueObjectFields(1).build(),
            new DomainModelVisibility(), rezeptionCalls);

        // then
        assertThat(byDefault).doesNotContain(zeitraumBox).contains("zeitraum:<VO> Zeitraum");
        assertThat(lowered).contains(zeitraumBox);
    }

    @Test
    void Should_ShowOnlyTheMethodsCalledInTheFlow_ByDefault_And_AllMethods_When_SwitchedOff() {

        // given: the check-out flow calls checkeGastAus of the application service, but not its other methods
        var visibility = new DomainModelVisibility().replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_COMMAND));

        // when
        String byDefault = generate(rezeptionMirror, DiagramStylingConfiguration.builder().build(), visibility, rezeptionCalls);
        String switchedOff = generate(rezeptionMirror,
            DiagramStylingConfiguration.builder().showOnlyFlowMethods(false).build(), visibility, rezeptionCalls);

        // then
        assertThat(applicationServiceBox(byDefault))
            .contains("checkeGastAus").doesNotContain("checkeGastEin", "aktualisiereGastdaten");
        assertThat(applicationServiceBox(switchedOff))
            .contains("checkeGastAus", "checkeGastEin", "aktualisiereGastdaten");
    }

    @Test
    void Should_ConnectAServiceToAServiceItCallsInTheFlow_When_SwitchedOn_And_NotByDefault() {

        // given: a hand-built call between two application services holding no field of each other
        var caller = domainMethod(RezeptionScenario.BUCHUNG_APPLICATION_SERVICE, RezeptionScenario.CHECK_OUT_METHOD_NAME);
        var called = domainMethod(ZIMMER_APPLICATION_SERVICE, "findeZimmerauslastung");
        var calls = DomainCalls.builder()
            .add(caller, List.of(new DomainCalls.CallSite(called, RezeptionScenario.BUCHUNG_APPLICATION_SERVICE, 1)))
            .build();
        var visibility = new DomainModelVisibility()
            .replaceIncludeFlowsFrom(Set.of(RezeptionScenario.CHECK_OUT_METHOD_FLOW_STARTING_POINT));

        // when
        String switchedOn = generate(rezeptionMirror,
            DiagramStylingConfiguration.builder().showFlowCallRelations(true).build(), visibility, calls);
        String byDefault = generate(rezeptionMirror, DiagramStylingConfiguration.builder().build(), visibility, calls);

        // then: directed from the caller to the called service
        assertThat(switchedOn.lines())
            .anyMatch(line -> line.startsWith("[<AS>BuchungApplicationService ")
                && line.contains("<<calls>> ZimmerApplicationService.findeZimmerauslastung")
                && line.endsWith("[<AS>ZimmerApplicationService <<ApplicationService>>]"));
        assertThat(byDefault).doesNotContain("<<calls>>");
    }

    private static DomainMethod domainMethod(String typeName, String methodName) {
        var type = rezeptionMirror.getDomainTypeMirror(typeName).orElseThrow();
        return new DomainMethod(typeName, type.getMethods().stream()
            .filter(method -> method.getName().equals(methodName))
            .findFirst()
            .orElseThrow());
    }

    private static String applicationServiceBox(String nomnoml) {
        int start = nomnoml.indexOf("[<AS> BuchungApplicationService");
        assertThat(start).as("box of the application service").isNotNegative();
        return nomnoml.substring(start, nomnoml.indexOf(']', start) + 1);
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

    @Test
    void Should_CountClassBoxesWithoutAggregateFrames() {
        String nomnoml = String.join("\n",
            "#.AF:visual=frame align=left",
            "[<AF> Booking Aggregate|",
            "  [<AR> Booking]",
            "]",
            "[<ND> BookingMapper]",
            "[<DS> BookingService]");

        assertThat(DiagrammerUtils.countClasses(nomnoml)).isEqualTo(3);
    }

    @Test
    void Should_CountAnAggregateFrameWithoutContentAsClassBox() {
        String nomnoml = String.join("\n",
            "#.AF:visual=frame align=left",
            "[<AF> Booking <<Aggregate>>]",
            "[<DS> BookingService]");

        assertThat(DiagrammerUtils.countClasses(nomnoml)).isEqualTo(2);
    }
}
