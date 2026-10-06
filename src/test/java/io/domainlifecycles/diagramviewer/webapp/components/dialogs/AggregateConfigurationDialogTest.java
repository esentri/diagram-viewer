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
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The section {@code Aggregates} of the visibility dialog switches - centrally for all aggregates of a diagram - whether
 * they are drawn as their frame only.
 */
class AggregateConfigurationDialogTest {

    /** held here: Vaadin keeps the current UI only weakly */
    private UI ui;
    private DiagramService diagramService;
    private Diagram diagram;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
        diagramService = mock(DiagramService.class);
        when(diagramService.updateModelAndImageAsync(any()))
            .thenAnswer(invocation -> new DiagramRendering(invocation.getArgument(0), new CompletableFuture<>()));
        diagram = Diagram.builder().name("Orders").diagramStylingConfiguration(DiagramStylingConfiguration.builder().build())
            .build();
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void Should_ShowTheAggregatesWithTheirContent_InTheVisibilityDialog() {

        // given
        var dialog = new VisibilityConfigurationDialog(diagramService);

        // when
        dialog.setDiagram(diagram);

        // then
        var aggregateSection = section(dialog, "Aggregates");
        assertThat(labels(aggregateSection)).startsWith("Show", "Frame only", "Fields", "Methods");
        assertThat(frameOnlyCheckbox(aggregateSection).getValue()).isFalse();
    }

    @Test
    void Should_SaveThatOnlyTheFramesOfTheAggregatesAreShown_When_ChangedInTheVisibilityDialog() {

        // given
        var dialog = new VisibilityConfigurationDialog(diagramService);
        dialog.setDiagram(diagram);

        // when
        frameOnlyCheckbox(section(dialog, "Aggregates")).setValue(true);
        save(dialog);

        // then: the other aggregate settings stay
        var styling = diagram.getDiagramStylingConfiguration();
        assertThat(styling.isShowOnlyAggregateFrames()).isTrue();
        assertThat(styling.isShowAggregates()).isTrue();
        assertThat(styling.isShowAggregateFields()).isTrue();
        assertThat(styling.isShowAggregateMethods()).isTrue();
    }

    @Test
    void Should_DisableTheFieldsMethodsAndInlinedValueObjects_When_OnlyTheFramesOfTheAggregatesAreShown() {

        // given
        var dialog = new VisibilityConfigurationDialog(diagramService);
        dialog.setDiagram(diagram);
        var aggregateSection = section(dialog, "Aggregates");
        assertThat(fieldsAndMethods(aggregateSection).map(Checkbox::isEnabled)).containsExactly(true, true);
        assertThat(inlinedValueObjectFields(aggregateSection).isEnabled()).isTrue();

        // when
        frameOnlyCheckbox(aggregateSection).setValue(true);

        // then: greyed out, but their values are kept
        assertThat(fieldsAndMethods(aggregateSection).map(Checkbox::isEnabled)).containsExactly(false, false);
        assertThat(fieldsAndMethods(aggregateSection).map(Checkbox::getValue)).containsExactly(true, true);
        assertThat(inlinedValueObjectFields(aggregateSection).isEnabled()).isFalse();
        assertThat(inlinedValueObjectFields(aggregateSection).getValue()).isEqualTo(2);

        // when
        frameOnlyCheckbox(aggregateSection).setValue(false);

        // then
        assertThat(fieldsAndMethods(aggregateSection).map(Checkbox::isEnabled)).containsExactly(true, true);
        assertThat(inlinedValueObjectFields(aggregateSection).isEnabled()).isTrue();
    }

    @Test
    void Should_DisableTheFieldsMethodsAndInlinedValueObjects_When_ADiagramShowingOnlyTheFramesIsRead() {

        // given
        diagram.getDiagramStylingConfiguration().setShowOnlyAggregateFrames(true);
        var dialog = new VisibilityConfigurationDialog(diagramService);

        // when
        dialog.setDiagram(diagram);

        // then
        var aggregateSection = section(dialog, "Aggregates");
        assertThat(fieldsAndMethods(aggregateSection).map(Checkbox::isEnabled)).containsExactly(false, false);
        assertThat(inlinedValueObjectFields(aggregateSection).isEnabled()).isFalse();
    }

    /**
     * The checkboxes of the items {@code Fields} and {@code Methods}.
     */
    private static Stream<Checkbox> fieldsAndMethods(AccordionPanel section) {
        return descendants(section.getElement())
            .filter(FormLayout.FormItem.class::isInstance)
            .filter(item -> List.of("Fields", "Methods").contains(item.getElement().getTextRecursively().trim()))
            .flatMap(item -> descendants(item.getElement()))
            .filter(Checkbox.class::isInstance).map(Checkbox.class::cast);
    }

    private static IntegerField inlinedValueObjectFields(AccordionPanel section) {
        return descendants(section.getElement())
            .filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
            .findFirst()
            .orElseThrow();
    }

    private static Checkbox frameOnlyCheckbox(AccordionPanel section) {
        return checkboxes(section)
            .filter(checkbox -> checkbox.getId().filter("show-only-aggregate-frames"::equals).isPresent())
            .findFirst()
            .orElseThrow();
    }

    private static AccordionPanel section(Component dialog, String summaryText) {
        return descendants(dialog.getElement())
            .filter(AccordionPanel.class::isInstance).map(AccordionPanel.class::cast)
            .filter(panel -> summaryText.equals(panel.getSummaryText()))
            .findFirst()
            .orElseThrow();
    }

    private static Stream<String> labels(AccordionPanel section) {
        return descendants(section.getElement())
            .filter(FormLayout.FormItem.class::isInstance)
            .map(item -> item.getElement().getTextRecursively().trim());
    }

    private static Stream<Checkbox> checkboxes(AccordionPanel section) {
        return descendants(section.getElement()).filter(Checkbox.class::isInstance).map(Checkbox.class::cast);
    }

    private static void save(Component dialog) {
        descendants(dialog.getElement())
            .filter(Button.class::isInstance).map(Button.class::cast)
            .filter(button -> "Save".equals(button.getText()))
            .findFirst()
            .orElseThrow()
            .click();
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
            Stream.concat(element.getChildren(), virtualChildren).flatMap(AggregateConfigurationDialogTest::descendants));
    }
}
