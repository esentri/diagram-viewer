package io.domainlifecycles.diagramviewer.rest.api.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.io.IOException;

public class DomainModelDeserializer extends JsonDeserializer<DomainMirror> {
    @Override
    public DomainMirror deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String domainModelJson = p.getCodec().readTree(p).toString();

        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.deserialize(domainModelJson);
    }
}
