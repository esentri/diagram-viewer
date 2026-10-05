package io.domainlifecycles.diagramviewer.rest.api;

import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorUploadPayload;
import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorUploadPayloadReader;
import io.domainlifecycles.diagramviewer.security.ApiKeyAuthFilter;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
        byte[] domainMirrorGz = new byte[] {1, 2, 3};
        DomainMirrorUploadPayload payload = new DomainMirrorUploadPayload(domainMirrorGz, null, List.of());

        when(payloadReader.read(any())).thenReturn(payload);
        doNothing().when(projectService).createOrUpdateDomainModel(projectName, domainMirrorGz, null, List.of());

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", projectName)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"domainMirror\": {}}"));

        // then
        result.andExpect(status().isOk());
        verify(projectService).createOrUpdateDomainModel(projectName, domainMirrorGz, null, List.of());
    }

    @Test
    void Should_CreateOrUpdateDomainModel_When_DomainCallsArePresent() throws Exception {

        // given
        String projectName = "testProjectName";
        byte[] domainMirrorGz = new byte[] {1, 2, 3};
        byte[] domainCallsGz = new byte[] {4, 5, 6};
        DomainMirrorUploadPayload payload = new DomainMirrorUploadPayload(domainMirrorGz, domainCallsGz, List.of("test.package"));

        when(payloadReader.read(any())).thenReturn(payload);
        doNothing().when(projectService).createOrUpdateDomainModel(projectName, domainMirrorGz, domainCallsGz, List.of("test.package"));

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", projectName)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"domainMirror\": {}, \"domainCalls\": {}, \"domainModelPackages\": [\"test.package\"]}"));

        // then
        result.andExpect(status().isOk());
        verify(projectService).createOrUpdateDomainModel(projectName, domainMirrorGz, domainCallsGz, List.of("test.package"));
    }
}
