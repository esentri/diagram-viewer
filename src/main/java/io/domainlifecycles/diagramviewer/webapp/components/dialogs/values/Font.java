package io.domainlifecycles.diagramviewer.webapp.components.dialogs.values;

public enum Font {
    HELVETICA("helvetica", "Helvetica"),
    ARIAL("arial", "Arial");

    private final String nomnomlValue;
    private final String displayValue;

    Font(String nomnomlValue, String displayValue) {
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
