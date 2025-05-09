package io.domainlifecycles.diagramviewer.rest.api.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.domainlifecycles.diagramviewer.rest.api.model.DomainMirrorUploadDto;
import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import java.io.IOException;
import java.util.Set;

public class DomainMirrorUploadDtoDeserializer extends JsonDeserializer<DomainMirrorUploadDto> {
    @Override
    public DomainMirrorUploadDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        ObjectMapper o = new ObjectMapper();
        TreeNode jsonTreeNode = p.getCodec().readTree(p);

        final String domainModelPackagesJson = jsonTreeNode.get("domainModelPackages").toString();
        Set<String> domainModelPackages = o.readValue(domainModelPackagesJson,
            o.getTypeFactory().constructCollectionType(Set.class, String.class));

        final String domainMirrorJson = jsonTreeNode.get("domainMirror").toString();
        DomainMirror domainMirror = domainSerializer.deserialize(domainMirrorJson);

        return new DomainMirrorUploadDto(domainMirror, domainModelPackages);
    }
}
