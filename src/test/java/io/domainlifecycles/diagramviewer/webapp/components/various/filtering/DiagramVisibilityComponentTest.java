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
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.dom.Element;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.service.DiagramService;
import io.domainlifecycles.diagramviewer.webapp.session.SessionStorage;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import io.domainlifecycles.mirror.reflect.ReflectiveDomainMirrorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The element level filter offers the factories of a domain model as a group of their own, right after the domain
 * services. The fixture {@code fixtures.factory} has the factory {@code OrderFactory}, the domain service
 * {@code OrderService} and the aggregate {@code Order}.
 */
class DiagramVisibilityComponentTest {

    private static final DomainMirror MIRROR =
        new ReflectiveDomainMirrorFactory("fixtures.factory").initializeDomainMirror();

    @Test
    void Should_OfferTheFactoriesAsGroupOfTheirOwn_RightAfterTheDomainServices() {

        // given
        UUID projectId = UUID.randomUUID();
        SessionStorage sessionStorage = mock(SessionStorage.class);
        when(sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(projectId)).thenReturn(
            MIRROR.getAllDomainTypeMirrors().stream()
                .filter(type -> type.getTypeName().startsWith("fixtures.factory"))
                .toList());
        // the groups opened, so they list their types
        when(sessionStorage.isDomainTypeSettingOpen(any())).thenReturn(true);
        var component = new DiagramVisibilityComponent(sessionStorage, mock(DiagramService.class));

        // when
        component.setDiagram(Diagram.builder().project(Project.builder().id(projectId).build()).build());

        // then
        assertThat(groups(component).map(Details::getSummaryText))
            .containsSubsequence("DomainService", "Factory", "AggregateRoot");
        Details factories = groups(component).filter(group -> group.getSummaryText().equals("Factory")).findFirst()
            .orElseThrow();
        assertThat(listedTypes(factories)).containsExactly("fixtures.factory.OrderFactory");
    }

    @Test
    void Should_ShowTheEntriesOfAGroupWithTheSameHeight_SoThatALongListDoesNotJump() {

        // given
        var component = componentWithOpenedGroups();
        Details factories = groups(component).filter(group -> group.getSummaryText().equals("Factory")).findFirst()
            .orElseThrow();
        VirtualList<DomainTypeMirror> list = virtualList(factories);
        DomainTypeMirror orderFactory = MIRROR.getDomainTypeMirror("fixtures.factory.OrderFactory").orElseThrow();

        // when
        Component entry = component.createAndGetContentForDomainTypeAndMirror(orderFactory.getDomainType(), orderFactory);

        // then: the entries not fetched yet are drawn like a real one, and a name takes two lines with the full name
        // as tooltip
        assertThat(list.getPlaceholderItem()).isNotNull();
        Element name = descendants(entry.getElement())
            .filter(child -> child.getElement().getClassList().contains(DiagramVisibilityComponent.TYPE_NAME_CSS_CLASS))
            .findFirst().orElseThrow().getElement();
        assertThat(name.getTextRecursively()).isEqualTo("OrderFactory");
        assertThat(name.getAttribute("title")).isEqualTo("fixtures.factory.OrderFactory");
    }

    private static DiagramVisibilityComponent componentWithOpenedGroups() {
        UUID projectId = UUID.randomUUID();
        SessionStorage sessionStorage = mock(SessionStorage.class);
        when(sessionStorage.getAllDomainTypeMirrorsWithoutEnumsAndIds(projectId)).thenReturn(
            MIRROR.getAllDomainTypeMirrors().stream()
                .filter(type -> type.getTypeName().startsWith("fixtures.factory"))
                .toList());
        when(sessionStorage.isDomainTypeSettingOpen(any())).thenReturn(true);
        var component = new DiagramVisibilityComponent(sessionStorage, mock(DiagramService.class));
        component.setDiagram(Diagram.builder().project(Project.builder().id(projectId).build()).build());
        return component;
    }

    @SuppressWarnings("unchecked")
    private static VirtualList<DomainTypeMirror> virtualList(Details group) {
        return descendants(group.getElement())
            .filter(VirtualList.class::isInstance)
            .map(list -> (VirtualList<DomainTypeMirror>) list)
            .findFirst()
            .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static List<String> listedTypes(Details group) {
        return descendants(group.getElement())
            .filter(VirtualList.class::isInstance)
            .map(list -> (VirtualList<DomainTypeMirror>) list)
            .flatMap(list -> ((ListDataProvider<DomainTypeMirror>) list.getDataProvider()).getItems().stream())
            .map(DomainTypeMirror::getTypeName)
            .toList();
    }

    private static Stream<Component> descendants(Element element) {
        return Stream.concat(element.getComponent().stream(),
            element.getChildren().flatMap(DiagramVisibilityComponentTest::descendants));
    }

    private static Stream<Details> groups(Component component) {
        return component.getChildren().filter(Details.class::isInstance).map(Details.class::cast);
    }
}
