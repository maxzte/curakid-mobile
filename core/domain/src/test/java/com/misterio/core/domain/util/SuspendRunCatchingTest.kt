package com.misterio.core.domain.util

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuspendRunCatchingTest {

    @Test
    fun `suspendRunCatching returns success when block completes normally`() {
        val result = suspendRunCatching {
            "success"
        }

        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun `suspendRunCatching returns failure when standard exception is thrown`() {
        val exception = IllegalStateException("Standard error")
        val result = suspendRunCatching {
            throw exception
        }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test(expected = CancellationException::class)
    fun `suspendRunCatching rethrows CancellationException`() {
        suspendRunCatching {
            throw CancellationException("Coroutine cancelled")
        }
    }
}
