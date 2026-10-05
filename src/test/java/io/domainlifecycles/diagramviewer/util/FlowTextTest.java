package io.domainlifecycles.diagramviewer.util;

import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.jackson3.JacksonDomainCallsSerializer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the textual flow display on the "rezeption" scenario: check-out command → application service → booking
 * aggregate → event → listener freeing the room.
 */
class FlowTextTest {

    private static final String CHECK_OUT_SERVICE_METHOD =
        RezeptionScenario.BUCHUNG_APPLICATION_SERVICE + "#" + RezeptionScenario.CHECK_OUT_METHOD_NAME;

    private static final String ZIMMER_APPLICATION_SERVICE = "com.esentri.rezeption.application.zimmer.ZimmerApplicationService";

    private static DomainMirror mirror;
    private static DomainCalls calls;

    @BeforeAll
    static void initMirror() {
        mirror = new JacksonDomainSerializer(false).deserialize(RezeptionScenario.domainMirrorJson());
        calls = new JacksonDomainCallsSerializer(false).deserialize(RezeptionScenario.domainCallsJson(), mirror);
    }

    @Test
    void Should_ShowTheForwardFlowAsTreeBelowItsStart() {

        // when
        List<String> lines = texts(new FlowText(mirror, calls, List.of(RezeptionScenario.CHECK_OUT_COMMAND), List.of())
            .render(new FlowText.Options(false, 0, null)));

        // then
        assertThat(lines).startsWith(
            "▼ WHAT IT LEADS TO",
            "[Command] CheckeGastAus   ◀ start",
            "└─ BuchungApplicationService.checkeGastAus(CheckeGastAus)",
            "   ├─ CheckeGastAus.buchungsId()",
            "   ├─ BuchungRepository.findById(BuchungsId)",
            "   │  ├⇒ BuchungRepositoryJooq.findById(BuchungsId)",
            "   │  │  └─ [Aggregate] Buchung",
            "   │  └─ [Aggregate] Buchung",
            "   ├─ Buchung.checkeAus()",
            "   │  └─ [Event] GastAusgecheckt",
            "   │     └─ ZimmerFreigabeListener.aufGastAusgecheckt(GastAusgecheckt)");
    }

    @Test
    void Should_ShowTheBackwardFlowAsUpsideDownTreeAboveItsTarget() {

        // when
        List<String> lines = texts(new FlowText(mirror, calls, List.of(), List.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT))
            .render(new FlowText.Options(true, 0, null)));

        // then: read in call order, from top to bottom
        assertThat(lines).containsExactly(
            "▲ WHAT LEADS INTO IT",
            "   ┌─ BuchungApplicationService.checkeGastAus(CheckeGastAus)",
            "┌─ Buchung.checkeAus()",
            "[Event] GastAusgecheckt   ◀ target",
            "");
    }

    @Test
    void Should_ShowTheBackwardPartAboveTheForwardPart_And_LeaveOutTheStartThere_When_BothAreConfigured() {

        // when
        List<String> lines = texts(new FlowText(mirror, calls, List.of(RezeptionScenario.CHECK_OUT_COMMAND),
            List.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT)).render(new FlowText.Options(false, 0, null)));

        // then
        assertThat(lines.indexOf("▲ WHAT LEADS INTO IT")).isLessThan(lines.indexOf("▼ WHAT IT LEADS TO"));
        assertThat(lines.subList(0, lines.indexOf("▼ WHAT IT LEADS TO"))).noneMatch(line -> line.contains("[Command]"));
    }

    @Test
    void Should_SummarizeAccessors_When_Hidden() {

        // when
        FlowText flowText = new FlowText(mirror, calls, List.of(RezeptionScenario.CHECK_OUT_COMMAND), List.of());
        List<String> hidden = texts(flowText.render(new FlowText.Options(true, 0, null)));
        List<String> shown = texts(flowText.render(new FlowText.Options(false, 0, null)));

        // then
        assertThat(hidden).noneMatch(line -> line.contains("CheckeGastAus.buchungsId()"));
        assertThat(hidden).contains("   └─ … 2 accessors hidden");
        assertThat(shown).anyMatch(line -> line.contains("CheckeGastAus.buchungsId()"));
        assertThat(shown).noneMatch(line -> line.contains("hidden"));
    }

    @Test
    void Should_MarkStepsWithMoreBehindThem_When_TheDepthIsLimited() {

        // when
        List<String> lines = texts(new FlowText(mirror, calls, List.of(RezeptionScenario.CHECK_OUT_COMMAND), List.of())
            .render(new FlowText.Options(true, 2, null)));

        // then
        assertThat(lines).containsExactly(
            "▼ WHAT IT LEADS TO",
            "[Command] CheckeGastAus   ◀ start",
            "└─ BuchungApplicationService.checkeGastAus(CheckeGastAus)",
            "   ├─ BuchungRepository.findById(BuchungsId)   …",
            "   ├─ Buchung.checkeAus()   …",
            "   ├─ BuchungRepository.update(Buchung)   …",
            "   └─ … 2 accessors hidden",
            "");
    }

    @Test
    void Should_ShowOnlyThePathsToMatchingSteps_When_Searching() {

        // when
        List<FlowText.Line> lines = new FlowText(mirror, calls, List.of(RezeptionScenario.CHECK_OUT_COMMAND), List.of())
            .render(new FlowText.Options(true, 0, "GIBFREI"));

        // then
        assertThat(texts(lines)).containsExactly(
            "▼ WHAT IT LEADS TO",
            "[Command] CheckeGastAus   ◀ start",
            "└─ BuchungApplicationService.checkeGastAus(CheckeGastAus)",
            "   └─ Buchung.checkeAus()",
            "      └─ [Event] GastAusgecheckt",
            "         └─ ZimmerFreigabeListener.aufGastAusgecheckt(GastAusgecheckt)",
            "            └─ Zimmer.gibFrei(BuchungsId)",
            "");
        assertThat(lines).filteredOn(FlowText.Line::match).extracting(FlowText.Line::text)
            .containsExactly("            └─ Zimmer.gibFrei(BuchungsId)");
    }

    @Test
    void Should_SayNothingMatches_When_NoStepContainsTheSearchedText() {

        // when
        List<String> lines = texts(new FlowText(mirror, calls, List.of(RezeptionScenario.CHECK_OUT_COMMAND), List.of())
            .render(new FlowText.Options(true, 0, "nothing like this")));

        // then
        assertThat(lines).containsExactly("No step contains \"nothing like this\".");
    }

    @Test
    void Should_ReferToAStart_When_AnotherFlowReachesIt() {

        // when: the application service method is a second start, reached by the command already
        List<String> lines = texts(new FlowText(mirror, calls,
            List.of(RezeptionScenario.CHECK_OUT_COMMAND, CHECK_OUT_SERVICE_METHOD), List.of())
            .render(new FlowText.Options(true, 0, null)));

        // then: its flow is shown once, below its own start
        assertThat(lines).contains("└─ BuchungApplicationService.checkeGastAus(CheckeGastAus)   → see start");
        assertThat(lines).contains("BuchungApplicationService.checkeGastAus(CheckeGastAus)   ◀ start");
        assertThat(lines.stream().filter(line -> line.contains("BuchungRepositoryJooq.findById"))).hasSize(1);
    }

    @Test
    void Should_ExpandAStepOnce_And_ReferToItEverywhereElse() {

        // when: both targets are reached through the check-out method of the application service
        List<String> lines = texts(new FlowText(mirror, calls, List.of(),
            List.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT, RezeptionScenario.BUCHUNG_AGGREGATE + "#checkeAus"))
            .render(new FlowText.Options(false, 0, null)));

        // then
        assertThat(lines).containsExactly(
            "▲ WHAT LEADS INTO IT",
            "      ┌─ [Command] CheckeGastAus",
            "   ┌─ BuchungApplicationService.checkeGastAus(CheckeGastAus)   [1]",
            "┌─ Buchung.checkeAus()",
            "[Event] GastAusgecheckt   ◀ target",
            "",
            "┌─ BuchungApplicationService.checkeGastAus(CheckeGastAus)   → see [1]",
            "Buchung.checkeAus()   ◀ target",
            "");
    }

    @Test
    void Should_ListTargetsWithoutPredecessorsTogether() {

        // when: nothing in the scenario calls the application service
        List<String> lines = texts(new FlowText(mirror, calls, List.of(),
            List.of(ZIMMER_APPLICATION_SERVICE + "#findeZimmerauslastung"))
            .render(new FlowText.Options(true, 0, null)));

        // then
        assertThat(lines).containsExactly(
            "▲ WHAT LEADS INTO IT",
            "Nothing found leading into:",
            "   ZimmerApplicationService.findeZimmerauslastung(LocalDate, LocalDate, Zimmerkategorie)   ◀ target",
            "");
    }

    @Test
    void Should_NameFlowPointsMissingInTheDomainModel() {

        // when
        List<String> lines = texts(new FlowText(mirror, calls, List.of("com.example.Unknown"),
            List.of(RezeptionScenario.CHECK_OUT_COMMAND)).render(new FlowText.Options(true, 0, null)));

        // then: a command cannot be a backward target either
        assertThat(lines).startsWith(
            "Not found in the domain model: " + RezeptionScenario.CHECK_OUT_COMMAND,
            "Not found in the domain model: com.example.Unknown");
    }

    private static List<String> texts(List<FlowText.Line> lines) {
        return lines.stream().map(FlowText.Line::text).toList();
    }
}
