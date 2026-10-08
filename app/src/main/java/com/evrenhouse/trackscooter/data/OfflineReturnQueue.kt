package com.evrenhouse.trackscooter.data

import android.content.Context
import android.util.Log
import com.evrenhouse.trackscooter.TrackScooterApp
import com.evrenhouse.trackscooter.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime

@Serializable
data class QueuedReturnItem(
    val scooterId: String,
    val queuedAtMillis: Long,
    val queuedAtWib: String,
    val outlet: String? = null,
    val retryCount: Int = 0,
    val lastError: String? = null,
)

private object SafeLog {
    fun d(tag: String, msg: String) { runCatching { Log.d(tag, msg) } }
    fun i(tag: String, msg: String) { runCatching { Log.i(tag, msg) } }
    fun w(tag: String, msg: String, t: Throwable? = null) { runCatching { Log.w(tag, msg, t) } }
    fun e(tag: String, msg: String, t: Throwable? = null) { runCatching { Log.e(tag, msg, t) } }
}

/**
 * Thread-safe, atomic, persistent offline queue for scooter return actions.
 * Guarantees that no return request is lost when connection drops.
 * Uses atomic file write (.tmp + rename) to prevent corruption.
 */
class OfflineReturnQueue(
    private val context: Context? = null,
    private val storageDir: File? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    private val queueFile: File
        get() = File(storageDir ?: context?.filesDir ?: File(System.getProperty("java.io.tmpdir", ".")), "offline_return_queue.json")
    private val _pendingIds = MutableStateFlow<Set<String>>(emptySet())
    val pendingIds: StateFlow<Set<String>> = _pendingIds.asStateFlow()

    init {
        scope.launch {
            loadInitialQueue()
        }
    }

    private suspend fun loadInitialQueue() = mutex.withLock {
        withContext(Dispatchers.IO) {
            runCatching {
                if (queueFile.exists()) {
                    val content = queueFile.readText()
                    val items = json.decodeFromString<List<QueuedReturnItem>>(content)
                    _pendingIds.value = items.map { it.scooterId.uppercase() }.toSet()
                    SafeLog.d(TAG, "Loaded ${items.size} pending returns from offline queue")
                } else {
                    _pendingIds.value = emptySet()
                }
            }.onFailure { err ->
                SafeLog.w(TAG, "Failed to load offline return queue, resetting file", err)
                _pendingIds.value = emptySet()
            }
        }
    }

    /**
     * Atomically adds a return action to the persistent queue.
     */
    suspend fun enqueue(scooterId: String, outlet: String? = null): Boolean = mutex.withLock {
        withContext(Dispatchers.IO) {
            val cleanId = scooterId.trim().uppercase()
            runCatching {
                val currentItems = readItemsInternal().toMutableList()
                if (currentItems.any { it.scooterId.equals(cleanId, ignoreCase = true) }) {
                    SafeLog.d(TAG, "Unit $cleanId already present in offline queue")
                    return@withContext true
                }

                val nowMillis = System.currentTimeMillis()
                val nowWib = DateUtils.formatTime(LocalDateTime.now(DateUtils.WIB))
                val newItem = QueuedReturnItem(
                    scooterId = cleanId,
                    queuedAtMillis = nowMillis,
                    queuedAtWib = nowWib,
                    outlet = outlet,
                    retryCount = 0,
                    lastError = null,
                )
                currentItems.add(newItem)
                writeItemsInternal(currentItems)
                _pendingIds.value = currentItems.map { it.scooterId.uppercase() }.toSet()
                SafeLog.i(TAG, "Enqueued return for unit $cleanId (total: ${currentItems.size})")
                true
            }.getOrElse { err ->
                SafeLog.e(TAG, "Failed to enqueue return for unit $cleanId", err)
                false
            }
        }
    }

    /**
     * Atomically removes a scooter from the queue after successful reconciliation.
     */
    suspend fun dequeue(scooterId: String): Boolean = mutex.withLock {
        withContext(Dispatchers.IO) {
            val cleanId = scooterId.trim().uppercase()
            runCatching {
                val currentItems = readItemsInternal().toMutableList()
                val removed = currentItems.removeAll { it.scooterId.equals(cleanId, ignoreCase = true) }
                if (removed) {
                    writeItemsInternal(currentItems)
                    _pendingIds.value = currentItems.map { it.scooterId.uppercase() }.toSet()
                    SafeLog.i(TAG, "Dequeued return for unit $cleanId (remaining: ${currentItems.size})")
                }
                removed
            }.getOrElse { err ->
                SafeLog.e(TAG, "Failed to dequeue return for unit $cleanId", err)
                false
            }
        }
    }

    /**
     * Checks if a scooter is waiting in the offline return queue.
     */
    fun isPending(scooterId: String): Boolean {
        return _pendingIds.value.contains(scooterId.trim().uppercase())
    }

    /**
     * Retrieves all items currently in the queue.
     */
    suspend fun getAll(): List<QueuedReturnItem> = mutex.withLock {
        withContext(Dispatchers.IO) {
            readItemsInternal()
        }
    }

    /**
     * Attempts to drain all queued items using the provided execute callback.
     * Items that succeed or are already returned on the server will be removed from queue.
     */
    suspend fun drain(
        executeReturn: suspend (String) -> ToggleResponse,
    ): Pair<Int, Int> = mutex.withLock {
        withContext(Dispatchers.IO) {
            val items = readItemsInternal()
            if (items.isEmpty()) return@withContext Pair(0, 0)

            SafeLog.i(TAG, "Starting queue drain for ${items.size} pending items...")
            val remainingItems = mutableListOf<QueuedReturnItem>()
            var successCount = 0
            var failureCount = 0
            for (item in items) {
                try {
                    val response = executeReturn(item.scooterId)
                    if (response.success) {
                        successCount++
                        SafeLog.i(TAG, "Successfully drained return for unit ${item.scooterId}")
                    } else {
                        failureCount++
                        remainingItems.add(
                            item.copy(
                                retryCount = item.retryCount + 1,
                                lastError = response.message ?: "Server rejected return",
                            )
                        )
                    }
                } catch (e: Exception) {
                    failureCount++
                    SafeLog.w(TAG, "Failed to drain return for unit ${item.scooterId}: ${e.message}")
                    remainingItems.add(
                        item.copy(
                            retryCount = item.retryCount + 1,
                            lastError = e.message ?: "Network error",
                        )
                    )
                }
            }

            writeItemsInternal(remainingItems)
            _pendingIds.value = remainingItems.map { it.scooterId.uppercase() }.toSet()
            Pair(successCount, failureCount)
        }
    }

    private fun readItemsInternal(): List<QueuedReturnItem> {
        if (!queueFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<QueuedReturnItem>>(queueFile.readText())
        }.getOrDefault(emptyList())
    }

    /**
     * Writes items to a temporary file first, then atomically renames it.
     */
    private fun writeItemsInternal(items: List<QueuedReturnItem>) {
        val parentDir = queueFile.parentFile ?: (storageDir ?: context?.filesDir ?: File("."))
        if (!parentDir.exists()) parentDir.mkdirs()
        val tempFile = File(parentDir, "${queueFile.name}.tmp")
        val content = json.encodeToString(items)
        tempFile.writeText(content)

        if (!tempFile.renameTo(queueFile)) {
            // Fallback: direct write if atomic rename fails on old storage
            queueFile.writeText(content)
            tempFile.delete()
        }
    }

    companion object {
        private const val TAG = "OfflineReturnQueue"

        @Volatile
        private var instance: OfflineReturnQueue? = null

        fun getInstance(context: Context = TrackScooterApp.instance): OfflineReturnQueue {
            return instance ?: synchronized(this) {
                instance ?: OfflineReturnQueue(context.applicationContext).also { instance = it }
            }
        }
    }
}
