package com.evrenhouse.trackscooter

import com.evrenhouse.trackscooter.data.OfflineReturnQueue
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.ToggleResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineReturnQueueTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var storageDir: File
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        storageDir = tempFolder.newFolder("queue_test")
    }

    @Test
    fun testEnqueueAndDeduplication() = runTest(testDispatcher) {
        val queue = OfflineReturnQueue(
            storageDir = storageDir,
            scope = testScope,
        )

        // Enqueue SD-01
        val enqueued1 = queue.enqueue("SD-01", "utara")
        assertTrue(enqueued1)
        assertTrue(queue.isPending("SD-01"))
        assertTrue(queue.isPending("sd-01")) // Case insensitive
        assertEquals(1, queue.getAll().size)

        // Enqueue SD-01 again -> should deduplicate without error
        val enqueuedDuplicate = queue.enqueue("SD-01", "utara")
        assertTrue(enqueuedDuplicate)
        assertEquals(1, queue.getAll().size)

        // Enqueue SD-02
        val enqueued2 = queue.enqueue("SD-02", "utara")
        assertTrue(enqueued2)
        assertEquals(2, queue.getAll().size)
        assertTrue(queue.isPending("SD-02"))
    }

    @Test
    fun testDequeue() = runTest(testDispatcher) {
        val queue = OfflineReturnQueue(
            storageDir = storageDir,
            scope = testScope,
        )

        queue.enqueue("SD-01", "utara")
        queue.enqueue("SD-02", "utara")
        assertEquals(2, queue.getAll().size)

        val dequeued = queue.dequeue("SD-01")
        assertTrue(dequeued)
        assertFalse(queue.isPending("SD-01"))
        assertTrue(queue.isPending("SD-02"))
        assertEquals(1, queue.getAll().size)
    }

    @Test
    fun testDrainWithSuccessfulReconciliation() = runTest(testDispatcher) {
        val queue = OfflineReturnQueue(
            storageDir = storageDir,
            scope = testScope,
        )

        queue.enqueue("SD-01", "utara")
        queue.enqueue("SD-02", "utara")
        assertEquals(2, queue.getAll().size)

        // Simulate draining where SD-01 succeeds and SD-02 is already returned (reconciled)
        val (success, failed) = queue.drain { id ->
            ToggleResponse(
                success = true,
                action = "return",
                message = "Unit $id berhasil dikembalikan",
                scooter = Scooter(id = id, type = "sd", status = ScooterStatus.AVAILABLE)
            )
        }

        assertEquals(2, success)
        assertEquals(0, failed)
        assertEquals(0, queue.getAll().size)
        assertFalse(queue.isPending("SD-01"))
        assertFalse(queue.isPending("SD-02"))
    }

    @Test
    fun testDrainWithTransientErrorKeepsItemInQueue() = runTest(testDispatcher) {
        val queue = OfflineReturnQueue(
            storageDir = storageDir,
            scope = testScope,
        )

        queue.enqueue("SD-01", "utara")

        // Simulate network failure
        val (success, failed) = queue.drain {
            ToggleResponse(
                success = false,
                message = "Network timeout"
            )
        }

        assertEquals(0, success)
        assertEquals(1, failed)
        assertEquals(1, queue.getAll().size)
        assertTrue(queue.isPending("SD-01"))
        assertEquals(1, queue.getAll().first().retryCount)
    }
}
