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

package io.domainlifecycles.diagramviewer.model;

import io.domainlifecycles.diagramviewer.model.viewer.DiagramStylingConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagramStylingConfigurationTest {

    @Test
    void Should_ShowTheMethodsButNotTheFieldsOfOutboundServices_ByDefault() {

        // when
        var built = DiagramStylingConfiguration.builder().build();
        var constructed = new DiagramStylingConfiguration();

        // then
        assertThat(built.isShowOutboundServiceMethods()).isTrue();
        assertThat(built.isShowOutboundServiceFields()).isFalse();
        assertThat(constructed.isShowOutboundServiceMethods()).isTrue();
        assertThat(constructed.isShowOutboundServiceFields()).isFalse();
    }

    @Test
    void Should_ShowFactoriesWithTheirMethodsAndCreatesRelations_ByDefault() {

        // when
        var built = DiagramStylingConfiguration.builder().build();
        var constructed = new DiagramStylingConfiguration();

        // then
        for (var configuration : java.util.List.of(built, constructed)) {
            assertThat(configuration.isShowFactories()).isTrue();
            assertThat(configuration.isShowFactoryFields()).isFalse();
            assertThat(configuration.isShowFactoryMethods()).isTrue();
            assertThat(configuration.isShowFactoryRelations()).isTrue();
            assertThat(configuration.getFactoryStyle()).isEqualTo("fill=#E0F0E0 bold");
        }
    }

    @Test
    void Should_DrawTheAggregatesWithTheirContent_ByDefault() {
        assertThat(DiagramStylingConfiguration.builder().build().isShowOnlyAggregateFrames()).isFalse();
        assertThat(new DiagramStylingConfiguration().isShowOnlyAggregateFrames()).isFalse();
    }

    @Test
    void Should_ShowOnlyTheMethodsCalledInTheFlows_ByDefault() {
        assertThat(DiagramStylingConfiguration.builder().build().isShowOnlyFlowMethods()).isTrue();
        assertThat(new DiagramStylingConfiguration().isShowOnlyFlowMethods()).isTrue();
    }

    @Test
    void Should_NotConnectClassesCallingEachOtherInTheFlows_ByDefault() {
        assertThat(DiagramStylingConfiguration.builder().build().isShowFlowCallRelations()).isFalse();
        assertThat(new DiagramStylingConfiguration().isShowFlowCallRelations()).isFalse();
    }

    @Test
    void Should_ShowValueObjectsOfUpToTwoFieldsInline_ByDefault() {
        assertThat(DiagramStylingConfiguration.builder().build().getMaxInlinedValueObjectFields()).isEqualTo(2);
        assertThat(new DiagramStylingConfiguration().getMaxInlinedValueObjectFields()).isEqualTo(2);
    }
}
