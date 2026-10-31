package com.misterio.core.database

import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.entity.ChildEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class ChildDaoContractTest {

    private val childDao: ChildDao = mockk(relaxed = true)

    @Test
    fun getChildById_returnsMatchingChild() = runTest {
        val childId = UUID.randomUUID()
        val expectedEntity = ChildEntity(
            id = childId,
            name = "Alex",
            birthDate = LocalDate.now(),
            gender = "MALE",
            isSynced = true
        )

        coEvery { childDao.getChildById(childId) } returns expectedEntity

        val result = childDao.getChildById(childId)

        assertNotNull(result)
        assertEquals("Alex", result?.name)
        assertEquals(childId, result?.id)
        coVerify(exactly = 1) { childDao.getChildById(childId) }
    }

    @Test
    fun getAllChildren_returnsFlowOfChildren() = runTest {
        val childId = UUID.randomUUID()
        val entities = listOf(
            ChildEntity(
                id = childId,
                name = "Sophia",
                birthDate = LocalDate.now(),
                gender = "FEMALE",
                isSynced = true
            )
        )

        coEvery { childDao.getAllChildren() } returns flowOf(entities)

        val result = childDao.getAllChildren().first()

        assertEquals(1, result.size)
        assertEquals("Sophia", result.first().name)
    }

    @Test
    fun insertChild_invokesDaoMethod() = runTest {
        val entity = ChildEntity(
            id = UUID.randomUUID(),
            name = "Liam",
            birthDate = LocalDate.now(),
            gender = "MALE",
            isSynced = false
        )

        childDao.insertChild(entity)

        coVerify(exactly = 1) { childDao.insertChild(entity) }
    }
}
