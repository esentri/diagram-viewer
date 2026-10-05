package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.model.viewer.Diagram;
import io.domainlifecycles.diagramviewer.repository.DiagramRepository;
import io.domainlifecycles.diagramviewer.security.ApiKeyAuthFilter;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ResourceController.class)
@WebMvcTest(controllers = ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "diagrams.location=src/test/resources")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    ApiKeyAuthFilter apiKeyAuthFilter;

    @MockitoBean
    DiagramRepository diagramRepository;

    private static final UUID PROJECT_ID = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID DIAGRAM_ID = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Test
    void Should_GetImageByDiagramName_When_TheNameIsUniqueInTheProject() throws Exception {

        // given: the image is stored under the diagram's id
        when(diagramRepository.findByProjectIdAndName(PROJECT_ID, "Aggregates"))
            .thenReturn(List.of(Diagram.builder().id(DIAGRAM_ID).name("Aggregates").build()));

        // when: requested by the diagram's name, as before images were named after the id
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", PROJECT_ID, "Aggregates.svg"));

        // then
        result.andExpect(status().isOk()).andExpect(content().string(containsString("<svg")));
    }

    @Test
    void Should_Reject_When_TheDiagramNameIsAmbiguousInTheProject() throws Exception {

        // given: two folders with a diagram "Aggregates" each
        when(diagramRepository.findByProjectIdAndName(PROJECT_ID, "Aggregates")).thenReturn(List.of(
            Diagram.builder().id(DIAGRAM_ID).name("Aggregates").build(),
            Diagram.builder().id(UUID.randomUUID()).name("Aggregates").build()));

        // when / then
        mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", PROJECT_ID, "Aggregates.svg"))
            .andExpect(status().isConflict());
    }

    @Test
    void Should_GetFile_WhenNoAdditionalRequestParametersAreSpecified() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg"));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(content().string(containsString("<svg")));
    }

    @Test
    void Should_GetFile_WhenAdditionalIgnoredRequestParametersAreSpecified() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "diagram.svg")
            .param("diagramLastModified", "ignored")
            .param("stylingLastModified", "ignored"));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(content().string(containsString("<svg")));
    }

    @Test
    void Should_ThrowDiagramViewerException_WhenDirectoryNameIsInvalid() throws Exception {

        // when
        assertThatThrownBy(() -> mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "\0", "diagram.svg")))
            .isInstanceOf(ServletException.class)
            .hasCauseInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("Location of requested file 'src/test/resources/\u0000/diagram.svg' is not a valid path.");
    }

    @Test
    void Should_ThrowDiagramViewerException_WhenFileIsNotFound() throws Exception {

        // when
        assertThatThrownBy(() -> mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "diagrams", "random.svg")))
            .isInstanceOf(ServletException.class)
            .hasCauseInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("No file found at '");
    }

    @Test
    void Should_ThrowDiagramViewerException_WhenDirectoryNameEscapesTheDiagramsDirectory() throws Exception {

        // when: ".." would resolve outside the configured diagrams directory, e.g. to read
        // 'src/test/build.gradle' instead of a file under 'src/test/resources'
        assertThatThrownBy(() -> mockMvc.perform(get("/api/resources/view/{directoryName}/{fileName}", "..", "build.gradle")))
            .isInstanceOf(ServletException.class)
            .hasCauseInstanceOf(DiagramViewerException.class)
            .hasMessageContaining("is outside the diagrams directory");
    }
}