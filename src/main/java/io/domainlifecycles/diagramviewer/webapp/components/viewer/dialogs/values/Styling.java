package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values;

public enum Styling {
    BOLD("bold", "Bold"),
    CENTER("center", "Center"),
    ITALIC("italic", "Italic"),
    LEFT("left", "Left"),
    UNDERLINE("underline", "Underline");

    private final String nomnomlValue;
    private final String displayValue;

    Styling(String nomnomlValue, String displayValue) {
        this.nomnomlValue = nomnomlValue;
        this.displayValue = displayValue;
    }

    public String getNomnomlValue() {
        return nomnomlValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
