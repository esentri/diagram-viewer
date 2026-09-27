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

package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorUploadPayload;
import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorUploadPayloadReader;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DomainMirrorUploadController.UPLOAD_DOMAIN_MIRROR_API_PATH)
public class DomainMirrorUploadController {

    public static final String UPLOAD_DOMAIN_MIRROR_API_PATH = "/api/upload/";

    private final ProjectService projectService;
    private final DomainMirrorUploadPayloadReader payloadReader;

    public DomainMirrorUploadController(ProjectService projectService, DomainMirrorUploadPayloadReader payloadReader) {
        this.projectService = projectService;
        this.payloadReader = payloadReader;
    }

    /**
     * Accepts a domain mirror upload, optionally including the result of a static analysis of the
     * domain classes ({@code DomainCalls}). Supports both the plain and the streaming (chunked
     * transfer encoded) upload sent by the DLC build plugin, as well as an optional
     * {@code Content-Encoding: gzip} compressed request body - both are handled transparently by
     * reading the request body as a plain stream rather than binding it via a fixed-size
     * {@code @RequestBody}.
     *
     * @param projectName the name of the project the upload belongs to
     * @param request the incoming upload request, whose body is read directly
     * @return an empty 200 OK response on success
     */
    @PutMapping("/domain-mirror/{projectName}")
    public ResponseEntity<String> createOrUpdateDomainModel(
        @PathVariable String projectName, HttpServletRequest request) throws IOException {

        DomainMirrorUploadPayload payload = payloadReader.read(request.getInputStream());
        projectService.createOrUpdateDomainModel(projectName, payload.domainMirrorGz(), payload.domainCallsGz());
        return ResponseEntity.ok().build();
    }
}
