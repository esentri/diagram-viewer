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

package io.domainlifecycles.diagramviewer.webapp.components.dialogs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;
import io.domainlifecycles.diagramviewer.service.BoundedContext;
import io.domainlifecycles.diagramviewer.service.BoundedContextAnalysisService.Kind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Before the Bounded Contexts are analyzed, the analyses to run are chosen in a dialog.
 */
class AnalyzeBoundedContextsDialogTest {

    /** held here: Vaadin keeps the current UI only weakly */
    private UI ui;
    private final List<Set<Kind>> analyzed = new ArrayList<>();
    private AnalyzeBoundedContextsDialog dialog;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
        dialog = dialog(true);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void Should_OfferAllAnalyses_AllChosen() {
        assertThat(checkboxes().map(Checkbox::getLabel))
            .containsExactly("Aggregates", "Aggregate Neighborhood", "Read Models", "Commands");
        assertThat(checkboxes().map(Checkbox::getValue)).containsOnly(true);
        assertThat(analyzeButton().isEnabled()).isTrue();
    }

    @Test
    void Should_RunTheChosenAnalyses_When_Confirmed() {

        // given
        checkbox("analysis-read-models").setValue(false);
        checkbox("analysis-commands").setValue(false);

        // when
        analyzeButton().click();

        // then
        assertThat(analyzed).containsExactly(Set.of(Kind.AGGREGATES, Kind.AGGREGATE_NEIGHBORHOOD));
    }

    @Test
    void Should_NotAnalyze_When_NoAnalysisIsChosen() {

        // when
        checkboxes().forEach(checkbox -> checkbox.setValue(false));

        // then
        assertThat(analyzeButton().isEnabled()).isFalse();

        // when
        checkbox("analysis-aggregates").setValue(true);

        // then
        assertThat(analyzeButton().isEnabled()).isTrue();
    }

    @Test
    void Should_DisableTheAnalysesShowingFlows_And_TellWhy_When_NoStaticAnalysisResultWasUploaded() {

        // when
        dialog = dialog(false);

        // then
        assertThat(checkbox("analysis-read-models").isEnabled()).isFalse();
        assertThat(checkbox("analysis-read-models").getValue()).isFalse();
        assertThat(checkbox("analysis-commands").isEnabled()).isFalse();
        assertThat(checkbox("analysis-commands").getValue()).isFalse();
        assertThat(checkbox("analysis-aggregates").isEnabled()).isTrue();
        assertThat(checkbox("analysis-aggregate-neighborhood").isEnabled()).isTrue();
        assertThat(hint()).hasValueSatisfying(hint -> assertThat(hint.getText()).contains("static analysis"));

        // when
        analyzeButton().click();

        // then
        assertThat(analyzed).containsExactly(Set.of(Kind.AGGREGATES, Kind.AGGREGATE_NEIGHBORHOOD));
    }

    @Test
    void Should_NotShowTheHint_When_AStaticAnalysisResultWasUploaded() {
        assertThat(hint()).isEmpty();
    }

    private AnalyzeBoundedContextsDialog dialog(boolean staticAnalysisAvailable) {
        return new AnalyzeBoundedContextsDialog(List.of(new BoundedContext("shop.orders", Optional.of("Orders"))),
            staticAnalysisAvailable, analyzed::add);
    }

    private Optional<Span> hint() {
        return descendants(dialog.getElement())
            .filter(Span.class::isInstance).map(Span.class::cast)
            .filter(span -> span.getId().filter(AnalyzeBoundedContextsDialog.STATIC_ANALYSIS_HINT_ID::equals).isPresent())
            .findFirst();
    }

    private Stream<Checkbox> checkboxes() {
        return descendants(dialog.getElement()).filter(Checkbox.class::isInstance).map(Checkbox.class::cast);
    }

    private Checkbox checkbox(String id) {
        return checkboxes().filter(checkbox -> checkbox.getId().filter(id::equals).isPresent()).findFirst().orElseThrow();
    }

    private Button analyzeButton() {
        return descendants(dialog.getElement())
            .filter(Button.class::isInstance).map(Button.class::cast)
            .filter(button -> "Analyze".equals(button.getText()))
            .findFirst()
            .orElseThrow();
    }

    /**
     * The components below the element - also the virtual children, e.g. the footer of a dialog.
     */
    private static Stream<Component> descendants(Element element) {
        Stream<Element> virtualChildren = element.getNode().hasFeature(VirtualChildrenList.class)
            ? StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                element.getNode().getFeature(VirtualChildrenList.class).iterator(), 0), false).map(Element::get)
            : Stream.empty();
        return Stream.concat(
            element.getComponent().stream(),
            Stream.concat(element.getChildren(), virtualChildren).flatMap(AnalyzeBoundedContextsDialogTest::descendants));
    }
}
