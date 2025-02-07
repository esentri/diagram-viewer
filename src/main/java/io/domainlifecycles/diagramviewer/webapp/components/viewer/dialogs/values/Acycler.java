package io.domainlifecycles.diagramviewer.webapp.components.viewer.dialogs.values;

public enum Acycler {
    GREEDY("greedy", "Greedy");

    private final String nomnomlValue;
    private final String displayValue;

    Acycler(String nomnomlValue, String displayValue) {
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
