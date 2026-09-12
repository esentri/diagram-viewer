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

package io.domainlifecycles.diagramviewer.rest.api.jackson;

import io.domainlifecycles.mirror.api.DomainMirror;
import java.util.List;

/**
 * The result of reading a domain mirror upload request body: the {@link DomainMirror} itself, the
 * optional result of a static analysis of the domain classes ({@code DomainCalls}, kept as its raw
 * JSON representation since it is only meaningful resolved against the {@link DomainMirror} it was
 * analyzed against), and the domain model packages the upload was restricted to.
 *
 * @param domainMirror         the uploaded domain mirror, never {@code null}
 * @param domainCallsJson      the raw JSON representation of the uploaded static analysis result
 *                             ({@code DomainCalls}), or {@code null} if none was uploaded
 * @param domainModelPackages  the domain model packages associated with the upload
 */
public record DomainMirrorUploadPayload(
    DomainMirror domainMirror, String domainCallsJson, List<String> domainModelPackages) {
}
