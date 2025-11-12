package app.db.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Converter(autoApply = false)
public class LocalDateTimeEpochMillisConverter implements AttributeConverter<LocalDateTime, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : attribute.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        return dbData == null ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(dbData), ZoneOffset.UTC);
    }
}
