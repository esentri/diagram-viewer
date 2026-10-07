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

import io.domainlifecycles.mirror.api.BoundedContextMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Bounded Context of a project's domain model, as mirrored by DLC: rooted at one package, with an optional name.
 *
 * @param packageName the root package of the Bounded Context
 * @param name        the human-readable name, if the Bounded Context's package annotation carries one
 */
public record BoundedContext(String packageName, Optional<String> name) {

    /**
     * @return what users see: the name, or the package if the Bounded Context has none
     */
    public String label() {
        return name.orElse(packageName);
    }

    /**
     * @param typeName a full qualified type name
     * @return {@code true} if the type belongs to this Bounded Context
     */
    public boolean contains(String typeName) {
        return typeName.startsWith(packageName + ".");
    }

    /**
     * @param domainMirror a domain mirror
     * @return its Bounded Contexts, sorted by label
     */
    public static List<BoundedContext> of(DomainMirror domainMirror) {
        if (domainMirror == null || domainMirror.getAllBoundedContextMirrors() == null) {
            return List.of();
        }
        return domainMirror.getAllBoundedContextMirrors().stream()
            .map(BoundedContext::of)
            .sorted(Comparator.comparing(BoundedContext::label, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private static BoundedContext of(BoundedContextMirror mirror) {
        return new BoundedContext(mirror.getPackageName(), mirror.getName());
    }

    /**
     * Tells real Bounded Contexts apart from DLC's fallback: without explicitly configured or annotated Bounded
     * Contexts, DLC mirrors each domain model package as a Bounded Context without a name - nothing a user would
     * want to filter by.
     *
     * @param boundedContexts     the Bounded Contexts of a domain model
     * @param domainModelPackages the packages the domain model was built from, {@code null} if unknown - then only
     *                            named or several Bounded Contexts count as declared
     * @return {@code true} if the Bounded Contexts are more than that fallback
     */
    public static boolean areDeclared(List<BoundedContext> boundedContexts, Collection<String> domainModelPackages) {
        if (boundedContexts.isEmpty()) {
            return false;
        }
        if (boundedContexts.stream().anyMatch(boundedContext -> boundedContext.name().isPresent())) {
            return true;
        }
        if (domainModelPackages == null || domainModelPackages.isEmpty()) {
            return boundedContexts.size() > 1;
        }
        Set<String> packages = boundedContexts.stream().map(BoundedContext::packageName).collect(Collectors.toSet());
        return !packages.equals(Set.copyOf(domainModelPackages));
    }
}
