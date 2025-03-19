package io.domainlifecycles.diagramviewer.kroki;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;

public enum FileType {
    PDF(".pdf"),
    SVG(".svg"),
    PNG(".png"),
    JPG( ".jpg"),
    NOMNOML(".nomnoml");

    private final String fileSuffix;

    FileType(String fileSuffix) {
        this.fileSuffix = fileSuffix;
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
}
