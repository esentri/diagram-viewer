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
 *  Copyright 2019-2025 the original author or authors.
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

package io.domainlifecycles.diagramviewer.exception;

import io.domainlifecycles.diagramviewer.scheduled.DiagramRegenerationTask.DiagramRegenerationError;
import java.util.List;
import java.util.stream.Collectors;

public class DiagramRegenerationTaskException extends RuntimeException {

    private final List<DiagramRegenerationError> errors;

    public DiagramRegenerationTaskException(List<DiagramRegenerationError> errors) {
        super(String.format("%s diagram regenerations have failed in scheduled task", errors.size()));
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return errors.stream()
            .map(error -> String.format("Regeneration of diagram '%s', UUID: '%s' failed. Cause: %s",
                error.getDiagramName(),
                error.getDiagramId(),
                error.getCaughtException().getMessage()))
            .collect(Collectors.joining("\n"));
    }
}
