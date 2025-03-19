package io.domainlifecycles.diagramviewer.webapp.components.dialogs.values;

public enum Ranker {
    LONGEST_PATH("longest-path", "Longest-Path"),
    NETWORK_SIMPLEX("network-simplex", "Network-Simplex"),
    TIGHT_TREE("tight-tree", "Tight-Tree");

    private final String nomnomlValue;
    private final String displayValue;

    Ranker(String nomnomlValue, String displayValue) {
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
