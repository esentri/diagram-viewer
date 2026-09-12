package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorUploadPayload;
import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorUploadPayloadReader;
import io.domainlifecycles.diagramviewer.security.ApiKeyAuthFilter;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.model.DomainModel;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(DomainMirrorUploadController.class)
@WebMvcTest(controllers = DomainMirrorUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class DomainMirrorUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    ProjectService projectService;

    @MockitoBean
    ApiKeyAuthFilter apiKeyAuthFilter;

    @MockitoBean
    DomainMirrorUploadPayloadReader payloadReader;

    @Test
    void Should_CreateOrUpdateDomainModel() throws Exception {

        // given
        String projectName = "testProjectName";
        DomainMirror domainMirror = new DomainModel(Map.of(), "test.package");
        DomainMirrorUploadPayload payload = new DomainMirrorUploadPayload(domainMirror, null, List.of());

        when(payloadReader.read(any())).thenReturn(payload);
        doNothing().when(projectService).createOrUpdateDomainModel(projectName, domainMirror, null);

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", projectName)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"domainMirror\": {}}"));

        // then
        result.andExpect(status().isOk());
        verify(projectService).createOrUpdateDomainModel(projectName, domainMirror, null);
    }

    @Test
    void Should_CreateOrUpdateDomainModel_When_DomainCallsArePresent() throws Exception {

        // given
        String projectName = "testProjectName";
        DomainMirror domainMirror = new DomainModel(Map.of(), "test.package");
        String domainCallsJson = "{\"callsByCaller\":[],\"diagnostics\":[]}";
        DomainMirrorUploadPayload payload = new DomainMirrorUploadPayload(domainMirror, domainCallsJson, List.of("test.package"));

        when(payloadReader.read(any())).thenReturn(payload);
        doNothing().when(projectService).createOrUpdateDomainModel(projectName, domainMirror, domainCallsJson);

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", projectName)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"domainMirror\": {}, \"domainCalls\": {}, \"domainModelPackages\": [\"test.package\"]}"));

        // then
        result.andExpect(status().isOk());
        verify(projectService).createOrUpdateDomainModel(projectName, domainMirror, domainCallsJson);
    }
}
