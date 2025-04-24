package io.domainlifecycles.diagramviewer.model.converter;

import io.domainlifecycles.mirror.api.DomainModel;
import io.domainlifecycles.mirror.serialize.api.DomainSerializer;
import io.domainlifecycles.mirror.serialize.api.JacksonDomainSerializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DomainModelConverter implements AttributeConverter<DomainModel, String> {

    @Override
    public String convertToDatabaseColumn(DomainModel domainModel) {
        if (domainModel == null) return null;

        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.serialize(domainModel);
    }

    @Override
    public DomainModel convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;

        DomainSerializer domainSerializer = new JacksonDomainSerializer(false);
        return domainSerializer.deserialize(dbValue);
    }
}