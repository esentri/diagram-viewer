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

import io.domainlifecycles.diagramviewer.model.viewer.Project;
import io.domainlifecycles.diagramviewer.model.viewer.ProjectDomainMirror;
import io.domainlifecycles.diagramviewer.webapp.components.dialogs.values.UploadFileType;
import io.domainlifecycles.mirror.api.AggregateRootMirror;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.api.DomainTypeMirror;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProjectDomainMirrorService {

    ProjectDomainMirror getByProjectId(final UUID projectId);

    List<DomainTypeMirror> getAllDomainTypeMirrorsWithoutEnumsAndIds(final UUID projectId);

    List<AggregateRootMirror> getAllAggregateRootMirrors(final UUID projectId);

    ProjectDomainMirror createOrUpdate(final Project project, Set<String> domainModelPackages, Path pathToFile, UploadFileType uploadFileType);

    ProjectDomainMirror createOrUpdate(final Project project, DomainMirror domainMirror);

    void delete(final UUID projectId);
}
