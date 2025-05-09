package io.domainlifecycles.diagramviewer.rest.api.jackson;

import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer externalDataDeserializerCustomizer() {
        return builder -> builder
            .deserializerByType(DomainMirrorUploadDto.class, new DomainMirrorUploadDtoDeserializer());
    }
}
