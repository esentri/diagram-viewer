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

import java.util.List;

/**
 * The result of reading a domain mirror upload request body: the domain mirror and the optional result
 * of a static analysis of the domain classes ({@code DomainCalls}), both as gzip-compressed JSON, and
 * the domain model packages the upload was restricted to.
 * <p>
 * Both are kept compressed as they are stored, so the upload never holds their uncompressed JSON in
 * memory - several gigabytes for a large domain model. The domain mirror has been validated by
 * deserializing it once; the static analysis result is not validated on upload - it is only resolved
 * against the domain mirror when a flow filter first needs it.
 *
 * @param domainMirrorGz       the gzip-compressed JSON of the uploaded domain mirror, validated, never {@code null}
 * @param domainCallsGz        the gzip-compressed JSON of the uploaded static analysis result
 *                             ({@code DomainCalls}), or {@code null} if none was uploaded
 * @param domainModelPackages  the domain model packages associated with the upload
 */
public record DomainMirrorUploadPayload(
    byte[] domainMirrorGz, byte[] domainCallsGz, List<String> domainModelPackages) {
}
