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

package io.domainlifecycles.diagramviewer.webapp.components.various.cards;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.service.DiagramDirectoryService;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DiagramCardTest {

    private static final long LIMIT = 1024 * 1024;

    private final DiagramDirectoryService diagramDirectoryService = mock(DiagramDirectoryService.class);
    private final Diagram diagram = Diagram.builder().name("Vertrag - ErstelleFolgevertragCommand").build();

    @Test
    void Should_ShowTheDiagramAsLazilyLoadedPreview_When_ItsImageIsSmall() {
        DiagramCard card = new DiagramCard(diagramDirectoryService, diagram, 300 * 1024, LIMIT, "api/resources/x.svg");

        Element image = images(card).findFirst().orElseThrow();
        assertThat(image.getAttribute("src")).isEqualTo("api/resources/x.svg");
        assertThat(image.getAttribute("loading")).isEqualTo("lazy");
    }

    @Test
    void Should_ShowAPlaceholder_When_ItsImageIsLarge() {
        DiagramCard card = new DiagramCard(diagramDirectoryService, diagram, 2_600_000, LIMIT, "api/resources/x.svg");

        assertThat(images(card)).isEmpty();
        assertThat(text(card)).contains("Large diagram (2.5 MB)").contains("Open it to view");
    }

    @Test
    void Should_ShowAPlaceholder_When_ItsImageIsNotRenderedYet() {
        DiagramCard card = new DiagramCard(diagramDirectoryService, diagram, -1, LIMIT, "api/resources/x.svg");

        assertThat(images(card)).isEmpty();
        assertThat(text(card)).contains("Not rendered yet");
    }

    /** the card's media slot is not among its component children, so the element tree is searched */
    private static Stream<Element> images(Component card) {
        return elements(card.getElement()).filter(element -> !element.isTextNode() && "img".equals(element.getTag()));
    }

    private static Stream<Element> elements(Element root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(DiagramCardTest::elements));
    }

    private static String text(Component root) {
        return root.getElement().getTextRecursively();
    }
}
