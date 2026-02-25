package io.domainlifecycles.diagramviewer.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.domainlifecycles.diagramviewer.rest.api.jackson.DomainMirrorDeserializer;
import io.domainlifecycles.diagramviewer.security.ApiKeyAuthFilter;
import io.domainlifecycles.diagramviewer.service.ProjectService;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.model.DomainModel;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.mirror.serialize.jackson2.JacksonDomainSerializer;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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

    @Test
    void Should_CreateOrUpdateDomainModel() throws Exception {

        // given
        String projectName = "testProjectName";
        DomainMirror domainMirror = new DomainModel(Map.of(), "test.package");

        DomainSerializer serializer = new JacksonDomainSerializer(false);
        String jsonBody = serializer.serialize(domainMirror);
        jsonBody = "{\"domainMirror\": " + jsonBody + "}";
        doNothing().when(projectService).createOrUpdateDomainModel(projectName, domainMirror);

        // when
        ResultActions result = mockMvc.perform(put("/api/upload/domain-mirror/{projectName}", projectName)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody));

        // then
        result.andExpect(status().isOk());
        verify(projectService).createOrUpdateDomainModel(projectName, domainMirror);
    }

    @Configuration
    public static class JacksonConfig {
        @Bean
        public SimpleModule domainMirrorModule() {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(DomainMirror.class, new DomainMirrorDeserializer());
            return module;
        }
    }
}
