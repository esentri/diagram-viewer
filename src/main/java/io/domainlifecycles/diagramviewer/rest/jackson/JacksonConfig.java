package io.domainlifecycles.diagramviewer.rest.jackson;

import io.domainlifecycles.mirror.api.DomainModel;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer externalDataDeserializerCustomizer() {
        return builder -> builder
            .deserializerByType(DomainModel.class, new DomainModelDeserializer());
    }
}
