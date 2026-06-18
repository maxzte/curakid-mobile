package com.misterio.core.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.network.api.AssessmentApi
import com.misterio.core.network.api.ChildApi
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.api.MeasurementApi
import com.misterio.core.network.dto.ChildResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SyncWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)
    
    private val childDao = mockk<ChildDao>(relaxed = true)
    private val measurementDao = mockk<MeasurementDao>(relaxed = true)
    private val eventDao = mockk<EventDao>(relaxed = true)
    private val assessmentDao = mockk<AssessmentDao>(relaxed = true)
    
    private val childApi = mockk<ChildApi>(relaxed = true)
    private val measurementApi = mockk<MeasurementApi>(relaxed = true)
    private val eventApi = mockk<EventApi>(relaxed = true)
    private val assessmentApi = mockk<AssessmentApi>(relaxed = true)

    private lateinit var worker: SyncWorker

    @Before
    fun setup() {
        worker = SyncWorker(
            context,
            workerParams,
            childDao,
            measurementDao,
            eventDao,
            assessmentDao,
            childApi,
            measurementApi,
            eventApi,
            assessmentApi
        )
    }

    @Test
    fun `doWork returns Success when no data to sync`() = runTest {
        coEvery { childDao.getUnsyncedChildren() } returns emptyList()
        coEvery { measurementDao.getUnsyncedMeasurements() } returns emptyList()
        coEvery { eventDao.getAllUnsyncedEvents() } returns emptyList()
        coEvery { eventDao.getPendingDeleteEvents() } returns emptyList()
        coEvery { assessmentDao.getUnsyncedAssessments() } returns emptyList()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `syncChildren handles creation and update fallback`() = runTest {
        val childId = UUID.randomUUID()
        val unsyncedChild = ChildEntity(
            id = childId,
            name = "Test Child",
            birthDate = LocalDate.now(),
            gender = "BOY",
            isSynced = false
        )
        
        coEvery { childDao.getUnsyncedChildren() } returns listOf(unsyncedChild)
        coEvery { measurementDao.getUnsyncedMeasurements() } returns emptyList()
        coEvery { eventDao.getAllUnsyncedEvents() } returns emptyList()
        coEvery { eventDao.getPendingDeleteEvents() } returns emptyList()
        coEvery { assessmentDao.getUnsyncedAssessments() } returns emptyList()

        // 1. Simulate creation failure due to conflict (400)
        coEvery { childApi.createChild(any()) } returns Response.error(400, mockk(relaxed = true))
        
        // 2. Simulate successful update fallback
        val childResponse = ChildResponse(
            id = childId,
            name = "Test Child",
            birthDate = LocalDate.now().toString(),
            gender = "BOY",
            isPremature = false,
            gestationalWeeks = null,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )
        coEvery { childApi.updateChild(childId, any()) } returns Response.success(childResponse)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { childApi.createChild(any()) }
        coVerify { childApi.updateChild(childId, any()) }
        coVerify { childDao.insertChild(any()) }
    }

    @Test
    fun `doWork retries on transient network error`() = runTest {
        val childId = UUID.randomUUID()
        val unsyncedChild = ChildEntity(
            id = childId,
            name = "Test Child",
            birthDate = LocalDate.now(),
            gender = "BOY",
            isSynced = false
        )
        
        coEvery { childDao.getUnsyncedChildren() } returns listOf(unsyncedChild)
        coEvery { measurementDao.getUnsyncedMeasurements() } returns emptyList()
        coEvery { eventDao.getAllUnsyncedEvents() } returns emptyList()
        coEvery { eventDao.getPendingDeleteEvents() } returns emptyList()
        coEvery { assessmentDao.getUnsyncedAssessments() } returns emptyList()

        // 1. Simulate network error (IOException mapped to NetworkError)
        coEvery { childApi.createChild(any()) } throws java.io.IOException("No internet")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork retries on transient server error`() = runTest {
        val childId = UUID.randomUUID()
        val unsyncedChild = ChildEntity(
            id = childId,
            name = "Test Child",
            birthDate = LocalDate.now(),
            gender = "BOY",
            isSynced = false
        )
        
        coEvery { childDao.getUnsyncedChildren() } returns listOf(unsyncedChild)
        coEvery { measurementDao.getUnsyncedMeasurements() } returns emptyList()
        coEvery { eventDao.getAllUnsyncedEvents() } returns emptyList()
        coEvery { eventDao.getPendingDeleteEvents() } returns emptyList()
        coEvery { assessmentDao.getUnsyncedAssessments() } returns emptyList()

        // Simulate server error (500)
        coEvery { childApi.createChild(any()) } returns Response.error(500, mockk(relaxed = true))

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
