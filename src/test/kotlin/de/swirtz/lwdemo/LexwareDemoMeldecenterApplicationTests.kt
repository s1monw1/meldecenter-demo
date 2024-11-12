package de.swirtz.lwdemo

import de.swirtz.lwdemo.controller.MeldecenterController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

/**
 * Verify application start up.
 */
@SpringBootTest
class LexwareDemoMeldecenterApplicationTests(
    @Autowired private val controller: MeldecenterController
) {

    @Test
    fun contextLoads() {
        assertNotNull(controller)
    }

}
