package de.swirtz.lwdemo.service.dispatching

import de.swirtz.lwdemo.createKVReportEntity
import de.swirtz.lwdemo.createPNUnifer
import de.swirtz.lwdemo.createRVReportEntity
import de.swirtz.lwdemo.data.ReportsRepository
import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.service.ReportsService
import de.swirtz.lwdemo.service.reporting.KVReportService
import de.swirtz.lwdemo.service.reporting.RVReportService
import de.swirtz.lwdemo.service.reporting.ReportServiceConfiguration
import de.swirtz.lwdemo.service.resolving.KVReportResolver
import de.swirtz.lwdemo.service.resolving.RVReportResolver
import de.swirtz.lwdemo.service.resolving.ReportsResolver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals

/**
 * Tests are slow due to Testcontainers.
 */
@DataJpaTest
@ActiveProfiles("postgres")
class ReportsDispatcherTests(
    @Autowired private val reportsRepository: ReportsRepository
) {

    private val pnUnifier = createPNUnifer()

    private val dispatcher = createDispatcher()

    fun createDispatcher(kvReportsShouldFail: Boolean = false) =
        ReportsDispatcher(
            ReportsService(reportsRepository),
            listOf(
                RVReportService(),
                KVReportService(
                    ReportServiceConfiguration(if (kvReportsShouldFail) 100 else 0, 0, false)
                )
            ),
            ReportsResolver(listOf(RVReportResolver(pnUnifier), KVReportResolver(pnUnifier)))
        )

    @BeforeEach
    fun clean() {
        reportsRepository.deleteAll()
    }

    @Test
    fun `dispatcher ends gracefully when no report is found`() {
        dispatcher.triggerReportDispatching()
    }

    @Test
    fun `dispatcher handles reports`() {
        val rvId = reportsRepository.save(createRVReportEntity()).id
        val kvId = reportsRepository.save(createKVReportEntity()).id

        dispatcher.triggerReportDispatching()

        rvId.assertStatus(ReportStatus.SENT)
        kvId.assertStatus(ReportStatus.SENT)
    }

    @Test
    fun `dispatcher handles failing reports`() {
        val dispatcher = createDispatcher(kvReportsShouldFail = true)
        val rvId = reportsRepository.save(createRVReportEntity()).id
        val kvId = reportsRepository.save(createKVReportEntity()).id

        dispatcher.triggerReportDispatching()

        rvId.assertStatus(ReportStatus.SENT)
        kvId.assertStatus(ReportStatus.FAILED)
    }

    @Test
    fun `dispatcher only considers first 3 reports at a time`() {
        val kvId1 = reportsRepository.save(createKVReportEntity()).id
        val kvId2 = reportsRepository.save(createKVReportEntity()).id
        val kvId3 = reportsRepository.save(createKVReportEntity()).id
        val kvId4 = reportsRepository.save(createKVReportEntity()).id

        dispatcher.triggerReportDispatching()

        kvId1.assertStatus(ReportStatus.SENT)
        kvId2.assertStatus(ReportStatus.SENT)
        kvId3.assertStatus(ReportStatus.SENT)
        kvId4.assertStatus(ReportStatus.AWAITING_SEND)
    }

    @Test
    fun `dispatcher only considers AWAITING_SENT reports`() {
        val kvId1 = reportsRepository.save(createKVReportEntity(status = ReportStatus.SENT)).id
        val kvId2 = reportsRepository.save(createKVReportEntity(status = ReportStatus.FAILED)).id
        val kvId3 = reportsRepository.save(createKVReportEntity(status = ReportStatus.IN_TRANSIT)).id
        val kvId4 = reportsRepository.save(createKVReportEntity(status = ReportStatus.AWAITING_SEND)).id

        dispatcher.triggerReportDispatching()

        kvId1.assertStatus(ReportStatus.SENT)
        kvId2.assertStatus(ReportStatus.FAILED)
        kvId3.assertStatus(ReportStatus.IN_TRANSIT)
        kvId4.assertStatus(ReportStatus.SENT)
    }

    private fun Int.assertStatus(status: ReportStatus) {
        assertEquals(status, reportsRepository.findById(this).get().status)
    }
}
