package de.swirtz.lwdemo.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Custom converter to support JPA (un)marshal jsonb data
 */
@Converter(autoApply = true)
class JsonbConverter : AttributeConverter<Map<String, Any>, String> {

    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, Any>?): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any> =
        if (dbData.isNullOrEmpty()) {
            emptyMap()
        } else {
            objectMapper.readValue(dbData)
        }
}
