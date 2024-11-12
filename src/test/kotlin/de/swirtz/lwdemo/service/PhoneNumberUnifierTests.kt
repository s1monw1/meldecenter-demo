package de.swirtz.lwdemo.service

import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class PhoneNumberUnifierTests {

    private val service = PhoneNumberUnifier(PhoneNumberUtil.getInstance())
    private val TEST_NUM = "+491731234567"

    @Test
    fun `number can be formatted with full cc`() {
        val phoneNumber = "0049 1731234567"

        val result = service.unify(phoneNumber)

        assertEquals(TEST_NUM, result)
    }

    @Test
    fun `number can be formatted without cc`() {
        val phoneNumber = "01731234567"

        val result = service.unify(phoneNumber)

        assertEquals(TEST_NUM, result)
    }

    @Test
    fun `number can be formatted with random spaces`() {
        val phoneNumber = "0173 123 456 7"

        val result = service.unify(phoneNumber)

        assertEquals(TEST_NUM, result)
    }

    @Test
    fun `number can be formatted with short cc`() {
        val phoneNumber = "+49 0173 123 456 7"

        val result = service.unify(phoneNumber)

        assertEquals(TEST_NUM, result)
    }

    @Test
    fun `number with letters will cause exception`() {
        val phoneNumber = "+49 017x 123 456 7"

        assertThrows<ReportValidationException> {
            service.unify(phoneNumber)
        }
    }
}
