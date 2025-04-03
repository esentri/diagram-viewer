package io.domainlifecycles.diagramviewer.webapp.components.dialogs.components;

import com.vaadin.flow.component.html.Input;

public class ColorPickerComponent extends Input {

    private static final String HTML_COLOR_INPUT_TYPE = "color";

    public ColorPickerComponent(String defaultValue) {
        this();
        setValue(defaultValue);
    }

    public ColorPickerComponent() {
        setType(HTML_COLOR_INPUT_TYPE);
    }
}
