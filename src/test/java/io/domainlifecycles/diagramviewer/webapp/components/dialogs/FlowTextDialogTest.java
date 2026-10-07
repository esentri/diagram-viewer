package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import io.domainlifecycles.diagramviewer.scenario.RezeptionScenario;
import io.domainlifecycles.diagramviewer.util.FlowText;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.jackson3.JacksonDomainSerializer;
import io.domainlifecycles.staticanalysis.DomainCalls;
import io.domainlifecycles.staticanalysis.serialize.jackson3.JacksonDomainCallsSerializer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowTextDialogTest {

    private static DomainMirror mirror;
    private static DomainCalls calls;

    /** held here: Vaadin keeps the current UI only weakly */
    private UI ui;
    private FlowTextDialog dialog;

    @BeforeAll
    static void initMirror() {
        mirror = new JacksonDomainSerializer(false).deserialize(RezeptionScenario.domainMirrorJson());
        calls = new JacksonDomainCallsSerializer(false).deserialize(RezeptionScenario.domainCallsJson(), mirror);
    }

    @BeforeEach
    void openDialog() {
        ui = new UI();
        UI.setCurrent(ui);
        dialog = new FlowTextDialog(mirror, calls, "Check-out", List.of(RezeptionScenario.CHECK_OUT_COMMAND),
            List.of(RezeptionScenario.GAST_AUSGECHECKT_EVENT));
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void Should_ShowTheCompleteFlowWithoutAccessors_When_ItIsSmall() {

        // then
        assertThat(dialog.depthSelect().getValue()).isEqualTo(FlowTextDialog.ALL_STEPS);
        assertThat(dialog.hideAccessorsCheckbox().getValue()).isTrue();
        assertThat(dialog.lines()).extracting(FlowText.Line::text)
            .contains("▲ WHAT LEADS INTO IT", "[Event] GastAusgecheckt   ◀ target", "▼ WHAT IT LEADS TO",
                "[Command] CheckeGastAus   ◀ start");
        assertThat(contentLineCount()).isEqualTo(dialog.lines().size());
    }

    @Test
    void Should_RenderAgain_When_TheOptionsChange() {

        // given
        int complete = dialog.lines().size();

        // when
        dialog.hideAccessorsCheckbox().setValue(false);

        // then
        assertThat(dialog.lines().size()).isGreaterThan(complete);

        // when
        dialog.searchField().setValue("gibFrei");

        // then
        assertThat(dialog.lines()).filteredOn(FlowText.Line::match).hasSize(1);
        assertThat(dialog.lines().size()).isLessThan(complete);
        assertThat(contentLineCount()).isEqualTo(dialog.lines().size());

        // when
        dialog.searchField().clear();
        dialog.depthSelect().setValue(3);

        // then
        assertThat(dialog.lines()).extracting(FlowText.Line::text).anyMatch(line -> line.endsWith("…"));
    }

    @Test
    void Should_CopyAndDownloadTheLinesShown_With_WhatTheDiagramIsRestrictedTo() {

        // when
        dialog.searchField().setValue("gibFrei");
        String text = dialog.plainText();

        // then
        assertThat(text).startsWith("Flow of diagram \"Check-out\"\n"
            + "Forward from: " + RezeptionScenario.CHECK_OUT_COMMAND + "\n"
            + "Backward to: " + RezeptionScenario.GAST_AUSGECHECKT_EVENT + "\n");
        assertThat(text).endsWith(FlowText.toText(dialog.lines()));
        assertThat(text).contains("└─ Zimmer.gibFrei(BuchungsId)");
    }

    private int contentLineCount() {
        return content().getChildCount();
    }

    private Element content() {
        return dialog.getChildren()
            .flatMap(component -> component.getElement().getChildren())
            .filter(element -> "flow-text".equals(element.getAttribute("id")))
            .findFirst()
            .orElseThrow();
    }
}
