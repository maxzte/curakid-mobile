package com.misterio.core.data.repository

import com.misterio.core.data.util.SyncManager
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.entity.ChildEntity
import com.misterio.core.domain.model.Gender
import com.misterio.core.network.api.ChildApi
import com.misterio.core.network.dto.ChildResponse
import com.misterio.curakid.core.common.AppDataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class ChildRepositoryImplTest {

    private val childApi: ChildApi = mockk(relaxed = true)
    private val childDao: ChildDao = mockk(relaxed = true)
    private val appDataStore: AppDataStore = mockk(relaxed = true)
    private val syncManager: SyncManager = mockk(relaxed = true)

    private lateinit var repository: ChildRepositoryImpl

    private val testChildId = UUID.randomUUID()
    private val testChildEntity = ChildEntity(
        id = testChildId,
        name = "Марія",
        birthDate = LocalDate.of(2023, 5, 10),
        gender = "FEMALE",
        isPremature = false,
        isSynced = true
    )

    @Before
    fun setUp() {
        every { appDataStore.lastChildrenSync } returns flowOf(0L)
        every { appDataStore.selectedChildId } returns flowOf(testChildId.toString())
        repository = ChildRepositoryImpl(childApi, childDao, appDataStore, syncManager)
    }

    @Test
    fun `getChildren returns cached children from Room DAO flow`() = runTest {
        every { childDao.getAllChildren() } returns flowOf(listOf(testChildEntity))

        val children = repository.getChildren().first()

        assertEquals(1, children.size)
        assertEquals("Марія", children.first().name)
        assertEquals(Gender.FEMALE, children.first().gender)
    }

    @Test
    fun `getSelectedChild returns cached child from Room DAO flow`() = runTest {
        every { childDao.getChildByIdFlow(testChildId) } returns flowOf(testChildEntity)

        val child = repository.getSelectedChild().first()

        assertNotNull(child)
        assertEquals(testChildId, child?.id)
        assertEquals("Марія", child?.name)
    }

    @Test
    fun `syncChildren fetches remote and updates Room via syncWithServer`() = runTest {
        val remoteDto = ChildResponse(
            id = testChildId,
            name = "Марія",
            birthDate = "2023-05-10",
            gender = "FEMALE",
            isPremature = false,
            gestationalWeeks = null,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )
        coEvery { childApi.getChildren() } returns retrofit2.Response.success(listOf(remoteDto))
        coEvery { childDao.getUnsyncedChildren() } returns emptyList()

        val result = repository.syncChildren(force = true)

        assertTrue(result.isSuccess)
        coVerify { childDao.syncWithServer(match { it.size == 1 && it.first().id == testChildId }) }
        coVerify { appDataStore.setLastChildrenSync(any()) }
    }

    @Test
    fun `addChild saves locally first as unsynced and triggers SyncManager`() = runTest {
        val result = repository.addChild(
            name = "Іван",
            birthDate = System.currentTimeMillis(),
            gender = Gender.MALE,
            isPremature = false,
            gestationalWeeks = null,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        assertTrue(result.isSuccess)
        coVerify { childDao.insertChild(match { !it.isSynced && it.name == "Іван" }) }
        coVerify { syncManager.scheduleSync() }
    }

    @Test
    fun `addChild preserves gestationalWeeks for premature child`() = runTest {
        val result = repository.addChild(
            name = "Лука",
            birthDate = System.currentTimeMillis(),
            gender = Gender.MALE,
            isPremature = true,
            gestationalWeeks = 33,
            allergies = emptyList(),
            chronicDiseases = emptyList(),
            pastDiseases = emptyList(),
            specialFeatures = emptyList()
        )

        assertTrue(result.isSuccess)
        coVerify { childDao.insertChild(match { it.isPremature && it.gestationalWeeks == 33 }) }
    }
}
