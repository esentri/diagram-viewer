package io.domainlifecycles.diagramviewer.webapp.components.dialogs.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadFileType {
    JAR(".jar", "application/java-archive"),
    JSON(".json", "application/json");

    private final String extension;
    private final String mimeType;

    public static UploadFileType findByMimeType(String mimeType) {
        for (UploadFileType uploadFileType : UploadFileType.values()) {
            if (uploadFileType.getMimeType().equalsIgnoreCase(mimeType)) {
                return uploadFileType;
            }
        }
        return null;
    }
}
