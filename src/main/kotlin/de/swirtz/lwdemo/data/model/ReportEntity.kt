package de.swirtz.lwdemo.data.model

import de.swirtz.lwdemo.data.JsonbConverter
import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.domain.ReportType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnTransformer
import java.time.LocalDateTime

@Entity
@Table(name = "reports")
data class ReportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,
    val reportingTime: LocalDateTime,
    val reportingClientId: String,

    @Convert(converter = JsonbConverter::class)
    @Column(name = "data", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    val data: Map<String, Any> = emptyMap(),
    @Convert(converter = JsonbConverter::class)
    @Column(name = "contact", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    val contact: Map<String, Any> = emptyMap(),

    @Enumerated(EnumType.STRING)
    val type: ReportType,
    @Enumerated(EnumType.STRING)
    val status: ReportStatus
)
