package io.domainlifecycles.diagramviewer.util;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DiagramFileUtilsTest {

    @Test
    void Should_AssembleDiagramUrl() {

        // given
        Instant diagramLastModified = Instant.now();
        Instant stylingLastModified = Instant.now();
        String diagramDirectory = "diagrams";
        String diagramFileName = "diagram.svg";

        // when
        String diagramUrl = DiagramFileUtils.assembleDiagramUrl(diagramLastModified, stylingLastModified,
            diagramDirectory, diagramFileName);

        // then
        assertThat(diagramUrl).isEqualTo(
            "/api/resources/" + diagramDirectory + "/" + diagramFileName +
                "?diagramLastModified=" + diagramLastModified +
                "&stylingLastModified=" + stylingLastModified);
    }
}