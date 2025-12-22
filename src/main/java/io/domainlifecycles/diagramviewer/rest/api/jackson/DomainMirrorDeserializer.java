package io.domainlifecycles.diagramviewer.rest.api.jackson;

import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@JacksonComponent
public class DomainMirrorDeserializer extends ValueDeserializer<DomainMirror> {

    @Override
    public DomainMirror deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.deserialize(p.getValueAsString());
    }
}
