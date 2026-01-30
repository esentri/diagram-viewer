package io.domainlifecycles.diagramviewer.model.converter;

import io.domainlifecycles.mirror.api.DomainMirror;
import io.domainlifecycles.mirror.serialize.DomainSerializer;
import io.domainlifecycles.mirror.serialize.Jackson3DomainSerializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DomainModelConverter implements AttributeConverter<DomainMirror, String> {

    @Override
    public String convertToDatabaseColumn(DomainMirror domainMirror) {
        if (domainMirror == null) return null;

        DomainSerializer domainSerializer = new Jackson3DomainSerializer(false);
        return domainSerializer.serialize(domainMirror);
    }

    @Override
    public DomainMirror convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;

        DomainSerializer domainSerializer = new Jackson3DomainSerializer(false);
        return domainSerializer.deserialize(dbValue);
    }
}