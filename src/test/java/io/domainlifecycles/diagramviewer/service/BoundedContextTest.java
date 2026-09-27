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

package io.domainlifecycles.diagramviewer.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoundedContextTest {

    private static final BoundedContext ORDERS = new BoundedContext("shop.orders", Optional.of("Order Management"));
    private static final BoundedContext SHIPPING = new BoundedContext("shop.shipping", Optional.empty());

    @Test
    void Should_UseNameAsLabel_And_FallBackToPackage() {
        assertThat(ORDERS.label()).isEqualTo("Order Management");
        assertThat(SHIPPING.label()).isEqualTo("shop.shipping");
    }

    @Test
    void Should_ContainOnlyTypesOfItsPackage() {
        assertThat(ORDERS.contains("shop.orders.domain.Order")).isTrue();
        assertThat(ORDERS.contains("shop.ordersarchive.Order")).isFalse();
    }

    @Test
    void Should_NotCountDlcFallbackAsDeclared() {
        // DLC's fallback: one nameless Bounded Context per domain model package
        assertThat(BoundedContext.areDeclared(List.of(new BoundedContext("shop", Optional.empty())), Set.of("shop")))
            .isFalse();
        assertThat(BoundedContext.areDeclared(List.of(), Set.of("shop"))).isFalse();
    }

    @Test
    void Should_CountNamedOrDifferentlyRootedContextsAsDeclared() {
        assertThat(BoundedContext.areDeclared(List.of(ORDERS), Set.of("shop"))).isTrue();
        assertThat(BoundedContext.areDeclared(List.of(SHIPPING), Set.of("shop"))).isTrue();
    }

    @Test
    void Should_CountOnlyNamedOrSeveralContextsAsDeclared_When_DomainModelPackagesAreUnknown() {
        assertThat(BoundedContext.areDeclared(List.of(SHIPPING), null)).isFalse();
        assertThat(BoundedContext.areDeclared(List.of(SHIPPING, new BoundedContext("shop.billing", Optional.empty())), null))
            .isTrue();
        assertThat(BoundedContext.areDeclared(List.of(ORDERS), null)).isTrue();
    }
}
