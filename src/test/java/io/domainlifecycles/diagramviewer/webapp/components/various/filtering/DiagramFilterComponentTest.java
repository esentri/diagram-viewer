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

package io.domainlifecycles.diagramviewer.webapp.components.various.filtering;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.dom.Element;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramRendering;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The advanced view filters limit "what leads to" ({@code Include ingoing connections to}) and "what does it lead to"
 * ({@code Include outgoing connections from}) in depth, 0 showing the complete path.
 */
class DiagramFilterComponentTest {

    private static final DomainMirror MIRROR =
        new ReflectiveDomainMirrorFactory("fixtures.factory").initializeDomainMirror();
    private static final String ORDER_SERVICE = "fixtures.factory.OrderService";

    /** held here: Vaadin keeps the current UI only weakly */
    private UI ui;
    private final UUID projectId = UUID.randomUUID();
    private SessionStorage sessionStorage;
    private DiagramService diagramService;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
        sessionStorage = mock(SessionStorage.class);
        when(sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(projectId)).thenReturn(
            MIRROR.getAllDomainTypeMirrors().stream()
                .filter(type -> type.getTypeName().startsWith("fixtures.factory"))
                .toList());
        diagramService = mock(DiagramService.class);
        when(diagramService.updateModelAndImageAsync(any()))
            .thenAnswer(invocation -> new DiagramRendering(invocation.getArgument(0), new CompletableFuture<>()));
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void Should_ShowTheCompletePathAndDisableTheDepths_When_NoClassIsSelected() {

        // when
        var component = component(diagram(new DomainModelVisibility()));

        // then
        assertThat(depthField(component, DiagramFilterComponent.INGOING_DEPTH_ID).getValue()).isZero();
        assertThat(depthField(component, DiagramFilterComponent.INGOING_DEPTH_ID).isEnabled()).isFalse();
        assertThat(depthField(component, DiagramFilterComponent.OUTGOING_DEPTH_ID).getValue()).isZero();
        assertThat(depthField(component, DiagramFilterComponent.OUTGOING_DEPTH_ID).isEnabled()).isFalse();
    }

    @Test
    void Should_ShowTheDepthOfTheDiagram_When_ClassesAreSelected() {

        // when
        var component = component(diagram(new DomainModelVisibility()
            .replaceIncludeConnectedToIngoingClassNames(Set.of(ORDER_SERVICE))
            .replaceIncludeConnectedDepths(2, 0)));

        // then
        assertThat(depthField(component, DiagramFilterComponent.INGOING_DEPTH_ID).getValue()).isEqualTo(2);
        assertThat(depthField(component, DiagramFilterComponent.INGOING_DEPTH_ID).isEnabled()).isTrue();
        assertThat(depthField(component, DiagramFilterComponent.OUTGOING_DEPTH_ID).isEnabled()).isFalse();
    }

    @Test
    void Should_SaveTheDepth_When_Changed() {

        // given
        Diagram diagram = diagram(new DomainModelVisibility()
            .replaceIncludeConnectedToOutgoingClassNames(Set.of(ORDER_SERVICE))
            .replaceIncludeConnectedDepths(1, 0));
        var component = component(diagram);

        // when
        depthField(component, DiagramFilterComponent.OUTGOING_DEPTH_ID).setValue(3);

        // then: the selection and the other depth stay
        var visibility = diagram.getDomainModelVisibility();
        assertThat(visibility.getIncludeConnectedToOutgoingDepth()).isEqualTo(3);
        assertThat(visibility.getIncludeConnectedToIngoingDepth()).isEqualTo(1);
        assertThat(visibility.getIncludeConnectedToOutgoingClassNames()).containsExactly(ORDER_SERVICE);
    }

    @Test
    void Should_SaveTheCompletePath_When_TheDepthIsCleared() {

        // given
        Diagram diagram = diagram(new DomainModelVisibility()
            .replaceIncludeConnectedToIngoingClassNames(Set.of(ORDER_SERVICE))
            .replaceIncludeConnectedDepths(2, 0));
        var component = component(diagram);

        // when
        depthField(component, DiagramFilterComponent.INGOING_DEPTH_ID).clear();

        // then
        assertThat(diagram.getDomainModelVisibility().getIncludeConnectedToIngoingDepth()).isZero();
    }

    @Test
    void Should_OfferAClassForTheOtherDirection_When_ItIsFollowedInOneDirection() {

        // when
        var component = component(diagram(new DomainModelVisibility()
            .replaceIncludeConnectedToIngoingClassNames(Set.of(ORDER_SERVICE))));

        // then
        assertThat(offeredClassNames(component, "Include outgoing connections from:")).contains(ORDER_SERVICE);
        assertThat(offeredClassNames(component, "Include Connections to:")).doesNotContain(ORDER_SERVICE);
        assertThat(offeredClassNames(component, "Exclude ingoing connections to:")).doesNotContain(ORDER_SERVICE);
    }

    @Test
    void Should_NotOfferAClassForIncludingAndExcludingAtOnce() {
        var visibility = new DomainModelVisibility()
            .replaceIncludeConnectedToOutgoingClassNames(Set.of("a.Included"))
            .replaceExcludeConnectedToIngoingClassNames(Set.of("a.Excluded"))
            .replaceIncludeConnectedToClassNames(Set.of("a.All"));

        assertThat(DiagramFilterComponent.unavailableClassNames(
            DiagramFilterComponent.ComboBoxVisibilityType.INCLUDE_CONNECTED_INGOING, visibility))
            .containsExactlyInAnyOrder("a.Excluded", "a.All");
        assertThat(DiagramFilterComponent.unavailableClassNames(
            DiagramFilterComponent.ComboBoxVisibilityType.EXCLUDE_CONNECTED_OUTGOING, visibility))
            .containsExactlyInAnyOrder("a.Included", "a.All");
        assertThat(DiagramFilterComponent.unavailableClassNames(
            DiagramFilterComponent.ComboBoxVisibilityType.INCLUDE_CONNECTED, visibility))
            .containsExactlyInAnyOrder("a.Included", "a.Excluded");
        assertThat(DiagramFilterComponent.unavailableClassNames(
            DiagramFilterComponent.ComboBoxVisibilityType.INVISIBLE, visibility)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static List<String> offeredClassNames(Component component, String label) {
        return descendants(component.getElement())
            .filter(MultiSelectComboBox.class::isInstance)
            .map(comboBox -> (MultiSelectComboBox<DomainTypeMirror>) comboBox)
            .filter(comboBox -> label.equals(comboBox.getLabel()))
            .findFirst()
            .orElseThrow()
            .getListDataView().getItems()
            .map(DomainTypeMirror::getTypeName)
            .toList();
    }

    private Diagram diagram(DomainModelVisibility visibility) {
        return Diagram.builder().project(Project.builder().id(projectId).build()).domainModelVisibility(visibility)
            .build();
    }

    private DiagramFilterComponent component(Diagram diagram) {
        var component = new DiagramFilterComponent(sessionStorage, diagramService);
        component.setDiagram(diagram);
        return component;
    }

    private static IntegerField depthField(Component component, String id) {
        return descendants(component.getElement())
            .filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
            .filter(field -> field.getId().filter(id::equals).isPresent())
            .findFirst()
            .orElseThrow();
    }

    private static Stream<Component> descendants(Element element) {
        return Stream.concat(element.getComponent().stream(),
            element.getChildren().flatMap(DiagramFilterComponentTest::descendants));
    }
}
