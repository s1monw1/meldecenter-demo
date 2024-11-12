package de.swirtz.lwdemo.configuration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PhoneNumberUtilBeanConfiguration {
    @Bean
    fun createPhoneNumberUtil(): PhoneNumberUtil = PhoneNumberUtil.getInstance()
}
