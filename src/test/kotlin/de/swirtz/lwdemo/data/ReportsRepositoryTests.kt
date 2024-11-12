package de.swirtz.lwdemo.data

import de.swirtz.lwdemo.createRVReportEntity
import de.swirtz.lwdemo.data.model.ReportEntity
import de.swirtz.lwdemo.domain.ReportStatus
import de.swirtz.lwdemo.domain.ReportType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests are slow due to Testcontainers.
 */
@DataJpaTest
@ActiveProfiles("postgres")
class ReportsRepositoryTests(
    @Autowired private val reportsRepository: ReportsRepository
) {

    @BeforeEach
    fun `clear database`() {
        reportsRepository.deleteAll()
    }

    @Test
    fun `entity can be stored`() {
        val entity = createRVReportEntity()

        reportsRepository.save(entity)

        val all: List<ReportEntity> = reportsRepository.findAll().toList()
        assertEquals(1, all.size)
    }

    @Test
    fun `entities can be fetched with custom query`() {
        repeat(10) {
            val entity = createRVReportEntity(clientId = "tester$it")
            reportsRepository.save(entity)
        }
        val all: List<ReportEntity> = reportsRepository.findAll().toList()
        assertEquals(10, all.size)

        val reports = reportsRepository.findByReportTypeLastXUnsent(ReportType.SV_RV.toString())
        assertEquals(3, reports.size)

    }

    @Test
    fun `entities with irrelevant status are not fetched with custom query`() {
        repeat(10) {
            val entity = createRVReportEntity(clientId = "tester$it", status = ReportStatus.SENT)
            reportsRepository.save(entity)
        }

        val reports = reportsRepository.findByReportTypeLastXUnsent(ReportType.SV_RV.toString())

        assertEquals(0, reports.size)
    }

    @Test
    fun `custom status query can be performed`() {
        repeat(10) {
            val entity = createRVReportEntity(clientId = "tester$it", status = ReportStatus.SENT)
            reportsRepository.save(entity)
        }

        val reports = reportsRepository.findByReportTypeLastXUnsent(
            ReportType.SV_RV.toString(),
            allowedStatus = listOf(ReportStatus.SENT.toString())
        )

        assertEquals(3, reports.size)
    }

    @Test
    fun `entities are fetched FIFO`() {
        val iterations = 10
        lateinit var latest: ReportEntity
        repeat(iterations) {
            val entity = createRVReportEntity(clientId = "tester$it")
            reportsRepository.save(entity)
            if (it == iterations - 1) {
                latest = entity
            }
        }

        val reports = reportsRepository.findByReportTypeLastXUnsent(ReportType.SV_RV.toString())

        reports.forEach {
            assertTrue(it.reportingTime.isBefore(latest.reportingTime))
        }
    }

}


