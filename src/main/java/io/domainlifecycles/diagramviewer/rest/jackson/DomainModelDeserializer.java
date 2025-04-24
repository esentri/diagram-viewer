package io.domainlifecycles.diagramviewer.rest.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.domainlifecycles.mirror.api.DomainModel;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;

public class DomainModelDeserializer extends JsonDeserializer<DomainModel> {
    @Override
    public DomainModel deserialize(JsonParser p, DeserializationContext ctxt) {
        String domainModelJson = p.getCodec().toString();

        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.deserialize(domainModelJson);
    }
}
