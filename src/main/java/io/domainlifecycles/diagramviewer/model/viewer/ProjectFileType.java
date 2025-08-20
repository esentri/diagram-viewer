package io.domainlifecycles.diagramviewer.model.viewer;

import lombok.Getter;

@Getter
public enum ProjectFileType {

    JAR(".jar"),
    JSON(".json");

    private final String fileTypeEnding;

    ProjectFileType(String fileTypeEnding) {
        this.fileTypeEnding = fileTypeEnding;
    }
}
