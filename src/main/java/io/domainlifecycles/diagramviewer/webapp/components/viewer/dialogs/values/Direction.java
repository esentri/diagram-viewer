package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values;

public enum Direction {
    UP("up", "Up"),
    DOWN("down", "Down");

    private final String nomnomlValue;
    private final String displayValue;

    Direction(String nomnomlValue, String displayValue) {
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
