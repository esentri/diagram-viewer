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

import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.util.DiagramFileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Renames diagram images stored under the diagram's name to the diagram's id, once at startup. Images used to be
 * named after the diagram, which only worked as long as names were unique within a project - they are unique within
 * a directory now, see {@link DiagramFileUtils#imagePath}.
 */
@Component
public class DiagramImageMigration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramImageMigration.class);

    private final DiagramRepository diagramRepository;
    private final String diagramsLocation;

    public DiagramImageMigration(DiagramRepository diagramRepository,
                                 @Value("${diagrams.location}") String diagramsLocation) {
        this.diagramRepository = diagramRepository;
        this.diagramsLocation = diagramsLocation;
    }

    @Override
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (Diagram diagram : diagramRepository.findAll()) {
            if (migrate(diagram)) {
                migrated++;
            }
        }
        if (migrated > 0) {
            LOGGER.info("Renamed {} diagram images to the id of their diagram.", migrated);
        }
    }

    boolean migrate(Diagram diagram) {
        Path legacy = DiagramFileUtils.legacyImagePath(diagramsLocation, diagram);
        Path current = DiagramFileUtils.imagePath(diagramsLocation, diagram);
        if (!Files.exists(legacy) || Files.exists(current)) {
            return false;
        }
        try {
            Files.move(legacy, current);
            return true;
        } catch (IOException e) {
            LOGGER.warn("Could not rename the image of diagram '{}' from {} to {}.", diagram.getName(), legacy, current, e);
            return false;
        }
    }
}
