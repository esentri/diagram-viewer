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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

@Tag("svg-zoom-viewer")
@JsModule("./svg-zoom-viewer/svg-zoom-viewer.ts")
public class SvgZoomViewer extends Component implements HasSize {

    public SvgZoomViewer(String src) {
        getElement().setProperty("src", src);
    }

    public void setSrc(String src) {
        getElement().setProperty("src", src);
    }

    public String getSrc() {
        return getElement().getProperty("src", "");
    }

    public void setDraggable(boolean draggable) {
        getElement().setProperty("draggable", draggable);
    }

    public boolean isDraggable() {
        return getElement().getProperty("draggable", true);
    }

    public void setWheelable(boolean wheelable) {
        getElement().setProperty("wheelable", wheelable);
    }

    public boolean isWheelable() {
        return getElement().getProperty("wheelable", true);
    }

    public void setPinchable(boolean pinchable) {
        getElement().setProperty("pinchable", pinchable);
    }

    public boolean isPinchable() {
        return getElement().getProperty("pinchable", true);
    }

    public void setBounds(boolean bounds) {
        getElement().setProperty("bounds", bounds);
    }

    public boolean isBounds() {
        return getElement().getProperty("bounds", false);
    }

    public void setShowControls(boolean showControls) {
        getElement().setProperty("showControls", showControls);
    }

    public boolean isShowControls() {
        return getElement().getProperty("showControls", true);
    }

    public void zoom(double ratio) {
        getElement().callJsFunction("zoom", ratio);
    }

    public void reset() {
        getElement().callJsFunction("reset");
    }

    public void moveTo(double x, double y) {
        getElement().callJsFunction("moveTo", x, y);
    }
}
