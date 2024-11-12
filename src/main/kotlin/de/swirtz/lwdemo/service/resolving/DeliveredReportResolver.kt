package de.swirtz.lwdemo.service.resolving

import de.swirtz.lwdemo.controller.dto.ClientContactDetailsDTO
import de.swirtz.lwdemo.controller.dto.ReportDTO
import de.swirtz.lwdemo.data.model.ReportEntity
import de.swirtz.lwdemo.domain.ClientContactDetails
import de.swirtz.lwdemo.domain.ClientPhoneNumber
import de.swirtz.lwdemo.domain.KVReport
import de.swirtz.lwdemo.domain.KVReportData
import de.swirtz.lwdemo.domain.RVReport
import de.swirtz.lwdemo.domain.RVReportData
import de.swirtz.lwdemo.domain.Report
import de.swirtz.lwdemo.domain.ReportId
import de.swirtz.lwdemo.domain.ReportType
import de.swirtz.lwdemo.service.PhoneNumberUnifier
import de.swirtz.lwdemo.service.ReportValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Sealed abstract base class for report resolvers that can convert client-provided [ReportDTO] into
 * concrete [Report] based on the given [ReportType]. The service also handles translation from application-stored
 * [ReportEntity] to concrete [Report]s.
 *
 * Implementations of this class handle validation of client-provided data and should throw [ReportValidationException]
 * to delegate validation errors back to the client.
 *
 * Phone numbers are being unified as part of the [convertFromClient] process.
 *
 */
sealed class DeliveredReportResolver<R : Report<*>>(
    open val phoneNumberUnifier: PhoneNumberUnifier
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Converts client-provided [ReportDTO] into [Report].
     */
    abstract fun convertFromClient(report: ReportDTO): Report<*>

    /**
     * Converts application-stored [ReportEntity] into [Report].
     */
    abstract fun convertFromPersistence(report: ReportEntity): Report<*>

    /**
     * Returns the [ReportType] the implementation can handle.
     */
    abstract fun resolvesFor(): ReportType

    /**
     * Extension function to get value from [ReportDTO.data] map or
     * throw [ReportValidationException] if not found.
     */
    fun <T : Any> ReportDTO.dataMapValueOrThrow(key: String): T =
        tryOrThrowValidationError("Key '$key' does not exist in report data.") {
            data.getValue(key) as T
        }

    /**
     * Extension function to get value from [ReportEntity.data] map or
     * throw [ReportValidationException] if not found.
     */
    fun <T : Any> ReportEntity.dataMapValueOrThrow(key: String): T =
        tryOrThrowValidationError("Key '$key' does not exist in report data.") {
            data.getValue(key) as T
        }

    /**
     * Extension function to get value from [ReportEntity.contact] map or
     * throw [ReportValidationException] if not found.
     */
    fun <T : Any> ReportEntity.contactMapValueOrThrow(key: String): T =
        tryOrThrowValidationError("Key '$key' does not exist in report contact.") {
            contact.getValue(key) as T
        }

    fun <T> tryOrThrowValidationError(errMessage: String, block: () -> T): T =
        try {
            block()
        } catch (e: RuntimeException) {
            logger.error(errMessage)
            throw ReportValidationException(errMessage)
        }

    fun ClientContactDetailsDTO.unifiedPhoneNumber(): ClientPhoneNumber =
        ClientPhoneNumber(phoneNumberUnifier.unify(phoneNumber))

}

/**
 * Implementation of [DeliveredReportResolver] for [ReportType.SV_KV] reports.
 */
@Component
class KVReportResolver(
    override val phoneNumberUnifier: PhoneNumberUnifier
) : DeliveredReportResolver<KVReport>(phoneNumberUnifier) {

    companion object {
        private const val KV_NUMBER = "kvNumber"
        private const val DATE = "date"
        private const val AMOUNT = "amount"
        private const val C_NAME = "name"
        private const val C_SUR_NAME = "surName"
        private const val C_ADDRESS = "address"
        private const val C_PHONE_NUMBER = "phoneNumber"
    }

    override fun convertFromClient(report: ReportDTO): KVReport =
        tryOrThrowValidationError("Could not convert report.") {
            KVReport(
                KVReportData(
                    report.dataMapValueOrThrow(KV_NUMBER),
                    LocalDate.parse(report.dataMapValueOrThrow(DATE)),
                    BigDecimal(report.dataMapValueOrThrow<String>(AMOUNT)),
                ),
                with(report.contact) {
                    ClientContactDetails(name, surName, address, unifiedPhoneNumber())
                }
            )
        }

    override fun convertFromPersistence(report: ReportEntity): KVReport =
        tryOrThrowValidationError("Could not convert report.") {
            KVReport(
                KVReportData(
                    report.dataMapValueOrThrow(KV_NUMBER),
                    LocalDate.parse(report.dataMapValueOrThrow(DATE)),
                    BigDecimal(report.dataMapValueOrThrow<String>(AMOUNT)),
                ),
                ClientContactDetails(
                    report.contactMapValueOrThrow(C_NAME),
                    report.contactMapValueOrThrow(C_SUR_NAME),
                    report.contactMapValueOrThrow(C_ADDRESS),
                    ClientPhoneNumber(report.contactMapValueOrThrow(C_PHONE_NUMBER)),
                ),
                ReportId(report.id)
            )
        }

    override fun resolvesFor() = ReportType.SV_KV
}

/**
 * Implementation of [DeliveredReportResolver] for [ReportType.SV_RV] reports.
 */
@Component
class RVReportResolver(
    override val phoneNumberUnifier: PhoneNumberUnifier
) : DeliveredReportResolver<RVReport>(phoneNumberUnifier) {

    companion object {
        private const val RV_NUMBER = "rvNumber"
        private const val DATE = "date"
        private const val AMOUNT = "amount"
        private const val C_NAME = "name"
        private const val C_SUR_NAME = "surName"
        private const val C_ADDRESS = "address"
        private const val C_PHONE_NUMBER = "phoneNumber"
    }

    override fun convertFromClient(report: ReportDTO): RVReport =
        tryOrThrowValidationError("Could not convert report.") {
            RVReport(
                RVReportData(
                    report.dataMapValueOrThrow(RV_NUMBER),
                    LocalDate.parse(report.dataMapValueOrThrow(DATE)),
                    BigDecimal(report.dataMapValueOrThrow<String>(AMOUNT))
                ),
                with(report.contact) {
                    ClientContactDetails(name, surName, address, unifiedPhoneNumber())
                }
            )
        }

    override fun convertFromPersistence(report: ReportEntity): RVReport =
        tryOrThrowValidationError("Could not convert report.") {
            RVReport(
                RVReportData(
                    report.dataMapValueOrThrow(RV_NUMBER),
                    LocalDate.parse(report.dataMapValueOrThrow(DATE)),
                    BigDecimal(report.dataMapValueOrThrow<String>(AMOUNT)),
                ),
                ClientContactDetails(
                    report.contactMapValueOrThrow(C_NAME),
                    report.contactMapValueOrThrow(C_SUR_NAME),
                    report.contactMapValueOrThrow(C_ADDRESS),
                    ClientPhoneNumber(report.contactMapValueOrThrow(C_PHONE_NUMBER)),
                ),
                ReportId(report.id)
            )
        }

    override fun resolvesFor() = ReportType.SV_RV

}

