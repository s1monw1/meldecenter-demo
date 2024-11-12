package de.swirtz.lwdemo

import de.swirtz.lwdemo.data.ReportsRepository
import de.swirtz.lwdemo.data.model.ReportEntity
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger

/**
 * A fake repository used in tests. Data is stored in-memory in a simple [List].
 *
 * This repository is *not* thread-safe.
 */
class ReportsRepositoryFake : ReportsRepository {
    private val nextId = AtomicInteger(1)
    private val entities: MutableList<ReportEntity> = mutableListOf()

    override fun <S : ReportEntity?> save(entity: S & Any): S & Any {
        entity.id = nextId.getAndAdd(1)
        entities.add(entity as ReportEntity)
        return entity
    }

    override fun findById(id: Int): Optional<ReportEntity> {
        return Optional.ofNullable<ReportEntity>(entities.find { it.id == id })
    }

    override fun findByReportTypeLastXUnsent(
        reportType: String,
        allowedStatus: List<String>,
        limit: Int
    ): List<ReportEntity> {
        throw UnsupportedOperationException()
    }

    override fun deleteAll() {
        entities.clear()
    }

    /** Functions below are not implemented and would throw an [UnsupportedOperationException] if called during tests.**/

    override fun <S : ReportEntity?> saveAll(entities: Iterable<S?>): Iterable<S?> {
        throw UnsupportedOperationException()
    }


    override fun existsById(id: Int): Boolean {
        throw UnsupportedOperationException()
    }

    override fun findAll(): Iterable<ReportEntity?> {
        throw UnsupportedOperationException()
    }

    override fun findAllById(ids: Iterable<Int?>): Iterable<ReportEntity?> {
        throw UnsupportedOperationException()
    }

    override fun count(): Long {
        throw UnsupportedOperationException()
    }

    override fun deleteById(id: Int) {
        throw UnsupportedOperationException()
    }

    override fun delete(entity: ReportEntity) {
        throw UnsupportedOperationException()
    }

    override fun deleteAllById(ids: Iterable<Int?>) {
        throw UnsupportedOperationException()
    }

    override fun deleteAll(entities: Iterable<ReportEntity?>) {
        throw UnsupportedOperationException()
    }

}
