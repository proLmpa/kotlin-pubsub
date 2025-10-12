package org.example.concurrent.HashMap

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

val concurrentMap = ConcurrentHashMap<String, Int>()

suspend fun safeUpdateCounter(key: String, count: Int) {
    repeat(count) {
        // Atomic Operation, compute()
        concurrentMap.compute(key) { _, value ->
            (value ?: 0) + 1
        }
    }
}

fun main(): kotlin.Unit = runBlocking {
    val key = "item_count"

    concurrentMap[key] = 0

    // 100 Coroutines created
    val jobs = List(100) {
        // Dispatchers.Default : Coroutine created in Shared Background Thread Pool
        launch(Dispatchers.Default) {
            safeUpdateCounter(key, 1_000_000)
        }
    }

    jobs.joinAll()

    println("Final Count : ${concurrentMap[key]}")
}