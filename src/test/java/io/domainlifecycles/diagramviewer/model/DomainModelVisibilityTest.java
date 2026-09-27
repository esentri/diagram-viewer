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

import io.domainlifecycles.diagramviewer.model.viewer.DomainModelVisibility;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelVisibilityTest {

    @Test
    void Should_NotRestrictPackages_When_NeitherPackagesNorBoundedContextsAreIncluded() {
        assertThat(new DomainModelVisibility().getEffectiveIncludedPackages()).isEmpty();
    }

    @Test
    void Should_ApplyBoundedContextsOrPackagesAlone_When_OnlyOneIsSet() {
        assertThat(new DomainModelVisibility()
            .replaceIncludedBoundedContextPackages(Set.of("shop.orders"))
            .getEffectiveIncludedPackages()).containsExactly("shop.orders");
        assertThat(new DomainModelVisibility()
            .replaceExplicitlyIncludedPackagesNames(Set.of("shop.orders.domain"))
            .getEffectiveIncludedPackages()).containsExactly("shop.orders.domain");
    }

    @Test
    void Should_KeepTheNarrowerOne_When_PackageAndBoundedContextOverlap() {
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceExplicitlyIncludedPackagesNames(Set.of("shop.orders.domain", "shop"))
            .replaceIncludedBoundedContextPackages(Set.of("shop.orders", "shop.shipping"));

        assertThat(visibility.getEffectiveIncludedPackages())
            .containsExactlyInAnyOrder("shop.orders.domain", "shop.orders", "shop.shipping");
    }

    @Test
    void Should_RestrictToNothing_When_PackageAndBoundedContextExcludeEachOther() {
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceExplicitlyIncludedPackagesNames(Set.of("shop.billing"))
            .replaceIncludedBoundedContextPackages(Set.of("shop.orders"));

        assertThat(visibility.getEffectiveIncludedPackages()).containsExactly(DomainModelVisibility.NO_PACKAGE);
    }

    @Test
    void Should_KeepIncludedBoundedContexts_When_OtherSettingsAreReplaced() {
        DomainModelVisibility visibility = new DomainModelVisibility()
            .replaceIncludedBoundedContextPackages(Set.of("shop.orders"))
            .replaceBlacklistedClassNames(Set.of("shop.orders.Legacy"))
            .replaceIncludeFlowsFrom(Set.of("shop.orders.PlaceOrder"))
            .replaceExplicitlyIncludedPackagesNames(Set.of("shop.orders.domain"));

        assertThat(visibility.getIncludedBoundedContextPackages()).containsExactly("shop.orders");
    }
}
