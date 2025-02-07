package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.components;

import com.vaadin.flow.component.html.Input;

public class ColorPickerComponent extends Input {

    private static final String HTML_COLOR_INPUT_TYPE = "color";

    public ColorPickerComponent(String defaultValue) {
        setType(HTML_COLOR_INPUT_TYPE);
        setValue(defaultValue);
    }
}
