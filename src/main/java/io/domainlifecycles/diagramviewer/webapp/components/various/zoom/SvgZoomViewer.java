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
