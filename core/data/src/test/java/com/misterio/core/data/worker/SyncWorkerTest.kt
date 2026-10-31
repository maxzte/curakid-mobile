package com.misterio.core.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.repository.DevelopmentRepository
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.sync.SyncResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)

    private val developmentRepository = mockk<DevelopmentRepository>(relaxed = true)
    private val childRepository = mockk<ChildRepository>(relaxed = true)
    private val measurementRepository = mockk<MeasurementRepository>(relaxed = true)
    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val assessmentRepository = mockk<AssessmentRepository>(relaxed = true)

    private lateinit var worker: SyncWorker

    @Before
    fun setup() {
        coEvery { developmentRepository.syncLifePeriods(any()) } returns Result.success(Unit)
        coEvery { childRepository.syncPending() } returns SyncResult.Success
        coEvery { measurementRepository.syncPending() } returns SyncResult.Success
        coEvery { eventRepository.syncPending() } returns SyncResult.Success
        coEvery { assessmentRepository.syncPending() } returns SyncResult.Success

        worker = SyncWorker(
            context,
            workerParams,
            developmentRepository,
            childRepository,
            measurementRepository,
            eventRepository,
            assessmentRepository
        )
    }

    @Test
    fun `doWork returns Success when all sync operations succeed`() = runTest {
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { developmentRepository.syncLifePeriods(force = true) }
        coVerify { childRepository.syncPending() }
        coVerify { measurementRepository.syncPending() }
        coVerify { eventRepository.syncPending() }
        coVerify { assessmentRepository.syncPending() }
    }

    @Test
    fun `doWork retries when any sync operation encounters transient network error`() = runTest {
        coEvery { childRepository.syncPending() } returns SyncResult.TransientFailure(
            DomainError.NetworkError("No connection")
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork retries when catalog sync encounters transient server error`() = runTest {
        coEvery { developmentRepository.syncLifePeriods(any()) } returns Result.failure(
            DomainError.ServerError("500")
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns Success when errors are permanent`() = runTest {
        coEvery { measurementRepository.syncPending() } returns SyncResult.PermanentFailure(
            DomainError.BadRequestError("400")
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}
