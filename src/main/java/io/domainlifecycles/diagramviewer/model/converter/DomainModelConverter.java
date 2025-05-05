package io.domainlifecycles.diagramviewer.model.converter;

import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DomainModelConverter implements AttributeConverter<DomainMirror, String> {

    @Override
    public String convertToDatabaseColumn(DomainMirror domainMirror) {
        if (domainMirror == null) return null;

        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.serialize(domainMirror);
    }

    @Override
    public DomainMirror convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;

        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.deserialize(dbValue);
    }
}