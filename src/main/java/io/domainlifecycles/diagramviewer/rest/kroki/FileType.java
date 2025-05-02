package io.domainlifecycles.diagramviewer.rest.kroki;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;

public enum FileType {
    PDF(".pdf", "PDF"),
    SVG(".svg", "SVG"),
    PNG(".png", "PNG"),
    JPG( ".jpg", "JPG"),
    NOMNOML(".nomnoml", "NOMNOML");

    private final String fileSuffix;
    private final String displayValue;

    FileType(String fileSuffix, String displayValue) {
        this.fileSuffix = fileSuffix;
        this.displayValue = displayValue;
    }

    public static FileType byName(final String name) {
        for (FileType fileType : FileType.values()) {
            if (fileType.name().equalsIgnoreCase(name)) {
                return fileType;
            }
        }
        throw DiagramViewerException.fail(String.format("Could not find matching FileType for file-name %s.", name));
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
