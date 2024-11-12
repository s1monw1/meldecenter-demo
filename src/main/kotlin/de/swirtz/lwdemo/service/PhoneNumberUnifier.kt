package de.swirtz.lwdemo.service

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service to verify and unify German phone numbers into E164 standard.
 */
@Service
class PhoneNumberUnifier(
    private val phoneNumberUtil: PhoneNumberUtil
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unifies phone numbers into E164 standard.
     *
     * @param phoneNumber the number to unify into E164
     * @return the unified phone number in E164 format
     * @throws IllegalArgumentException if [phoneNumber] is not a valid phone number
     */
    fun unify(phoneNumber: String): String {
        if (!verify(phoneNumber)) {
            "Phone number is not valid: $phoneNumber".let {
                logger.error(it)
                throw ReportValidationException(it)
            }

        }
        return phoneNumberUtil.format(
            phoneNumber.asGermanPhoneNumber(),
            PhoneNumberUtil.PhoneNumberFormat.E164
        )
    }


    private fun verify(phoneNumber: String): Boolean =
        try {
            phoneNumberUtil.isValidNumber(phoneNumber.asGermanPhoneNumber())
        } catch (e: NumberParseException) {
            false
        }


    private fun String.asGermanPhoneNumber(): Phonenumber.PhoneNumber =
        phoneNumberUtil.parse(this, "DE")

}
