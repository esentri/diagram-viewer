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

package io.domainlifecycles.diagramviewer.webapp.components.various.zoom;

import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.dom.Style.Overflow;
import io.domainlifecycles.diagramviewer.service.DiagramServiceImpl;
import java.time.Instant;

public class DiagramZoomComponentContainer extends FlexLayout {

    public DiagramZoomComponentContainer(final String projectId,
                                         final String diagramName,
                                         final Instant diagramLastModified,
                                         final Instant stylingLastModified) {
        setMinHeight("100%");
        setMaxHeight("100%");
        setSizeFull();

        getStyle().setMargin("0 1rem 0");
        getStyle().setOverflow(Overflow.HIDDEN);

        DiagramZoomComponent zoomComponent =
            new DiagramZoomComponent(
                diagramLastModified, stylingLastModified, projectId, diagramName + DiagramServiceImpl.SVG_FILE_SUFFIX);

        setFlexGrow(1, zoomComponent);
        add(zoomComponent);
    }
}
