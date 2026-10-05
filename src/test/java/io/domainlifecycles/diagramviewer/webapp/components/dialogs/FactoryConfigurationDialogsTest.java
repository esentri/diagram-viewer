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
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.components.ColorPickerComponent;
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
 * The visibility and the styling dialog have a section of their own for factories, which reads and saves the factory
 * settings of a diagram.
 */
class FactoryConfigurationDialogsTest {

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
    void Should_ShowTheFactorySettingsOfTheDiagram_InTheVisibilityDialog() {

        // given
        var dialog = new VisibilityConfigurationDialog(diagramService);

        // when
        dialog.setDiagram(diagram);

        // then: shown with their methods and creates relations, without their fields
        var factorySection = section(dialog, "Factory");
        assertThat(labels(factorySection)).containsExactly("Show", "Fields", "Methods", "Creates relations");
        assertThat(checkboxes(factorySection).map(Checkbox::getValue)).containsExactly(true, false, true, true);
    }

    @Test
    void Should_SaveTheFactorySettings_When_ChangedInTheVisibilityDialog() {

        // given
        var dialog = new VisibilityConfigurationDialog(diagramService);
        dialog.setDiagram(diagram);
        List<Checkbox> factoryCheckboxes = checkboxes(section(dialog, "Factory")).toList();

        // when
        factoryCheckboxes.get(0).setValue(false);
        factoryCheckboxes.get(1).setValue(true);
        factoryCheckboxes.get(3).setValue(false);
        save(dialog);

        // then
        var styling = diagram.getDiagramStylingConfiguration();
        assertThat(styling.isShowFactories()).isFalse();
        assertThat(styling.isShowFactoryFields()).isTrue();
        assertThat(styling.isShowFactoryMethods()).isTrue();
        assertThat(styling.isShowFactoryRelations()).isFalse();
    }

    @Test
    void Should_SaveTheFactoryColor_When_ChangedInTheStylingDialog() {

        // given
        var dialog = new StylingConfigurationDialog(diagramService);
        dialog.setDiagram(diagram);
        var colorPicker = descendants(section(dialog, "Factory").getElement())
            .filter(ColorPickerComponent.class::isInstance).map(ColorPickerComponent.class::cast).findFirst().orElseThrow();
        assertThat(colorPicker.getValue()).isEqualTo("#E0F0E0");

        // when
        colorPicker.setValue("#123456");
        save(dialog);

        // then: the color changes, the other styling stays
        assertThat(diagram.getDiagramStylingConfiguration().getFactoryStyle()).isEqualTo("fill=#123456 bold");
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
            Stream.concat(element.getChildren(), virtualChildren).flatMap(FactoryConfigurationDialogsTest::descendants));
    }
}
