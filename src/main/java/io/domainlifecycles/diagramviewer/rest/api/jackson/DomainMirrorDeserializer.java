package io.domainlifecycles.diagramviewer.rest.api.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DomainMirrorDeserializer extends JsonDeserializer<DomainMirror> {
    @Override
    public DomainMirror deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        JsonToken token = p.getCurrentToken();
        log.debug("Deserializing DomainMirror, token={}, text={}", token, p.getText());
        var val = p.getValueAsString();
        log.debug("Serialized DomainMirror: {}", val);
        // komplettes Objekt einlesen
        JsonNode node = p.getCodec().readTree(p);
        var mirrorNode = node.get("domainMirror");
        String json = mirrorNode.toString();

        log.debug("Serialized DomainMirror JSON: {}", json);
        return domainSerializer.deserialize(json);
    }
}
