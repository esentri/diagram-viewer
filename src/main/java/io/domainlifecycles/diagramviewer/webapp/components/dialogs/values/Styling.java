package io.domainlifecycles.diagramviewer.webapp.components.dialogs.values;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static Set<Styling> map(String[] stylingOptionsNomnomlValue) {
        return Arrays.stream(stylingOptionsNomnomlValue).map(Styling::of).collect(Collectors.toSet());
    }

    public static Styling of(final String nomnomlValue) {
        for (Styling styling : Styling.values()) {
            if (styling.nomnomlValue.equalsIgnoreCase(nomnomlValue)) {
                return styling;
            }
        }
        throw DiagramViewerException.fail("No Styling option found with nomnoml value: " + nomnomlValue);
    }

    public String getNomnomlValue() {
        return nomnomlValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
