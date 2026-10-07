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
package io.domainlifecycles.diagramviewer.webapp.components.various;

import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WrappableNameTest {

    @Test
    void Should_WrapAfterDotsAndUnderscores() {
        assertThat(WrappableName.segments("order.flow_diagram")).containsExactly("order.", "flow_", "diagram");
    }

    @Test
    void Should_WrapBeforeANewWordInCamelCase() {
        assertThat(WrappableName.segments("ZimmerApplicationService.checkeGastAus"))
            .containsExactly("Zimmer", "Application", "Service.", "checke", "Gast", "Aus");
    }

    @Test
    void Should_WrapBeforeANewWordFollowingADigitOrAnAcronym() {
        assertThat(WrappableName.segments("Order2Booking")).containsExactly("Order2", "Booking");
        assertThat(WrappableName.segments("HTTPServerAPI")).containsExactly("HTTP", "Server", "API");
    }

    @Test
    void Should_KeepNamesWithoutWrappingPlaces() {
        assertThat(WrappableName.segments("Short name")).containsExactly("Short name");
        assertThat(WrappableName.segments("")).containsExactly("");
        assertThat(WrappableName.segments("a")).containsExactly("a");
    }

    @Test
    void Should_ShowTheNameWithABreakOpportunityBetweenItsSegments() {
        Element span = WrappableName.create("ZimmerService.checke_GastAus").getElement();

        assertThat(span.getTextRecursively()).isEqualTo("ZimmerService.checke_GastAus");
        assertThat(span.getClassList()).contains("wrappable-name");
        // Zimmer|Service.|checke_|Gast|Aus
        assertThat(span.getChildren().filter(child -> !child.isTextNode()).map(Element::getTag))
            .containsExactly("wbr", "wbr", "wbr", "wbr");
    }
}
