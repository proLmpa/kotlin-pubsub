package org.example.concurrent.ArrayList

import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random
import kotlin.system.measureTimeMillis

val cowList = CopyOnWriteArrayList<Int>()

// Write Task : Lock, Create copy, Add element
suspend fun writeTask (count: Int) {
    repeat(count) {
        // When writing, Create and Append new copy of current List
        cowList.add(Random.nextInt(1, 100))
    }
}

// Read Task : Without Lock, Enter original array and iterate the whole elements
//              Never be blocked during the write task
suspend fun readTask(iterations: Int) {
    var sum = 0L
    repeat(iterations) {
        for(item in cowList) {
            sum += item
        }
    }
    println("Read Task Complete. Final Sum Check: $sum")
}

fun main() = runBlocking {
    val initialCount = 100_000
    (1..initialCount).forEach { cowList.add(it) }

    val numWriters = 10
    val writeOps = 1_000_000
    val numReaders = 20
    val readIterations = 5

    val totalTime = measureTimeMillis {
        val writeJobs = List(numWriters) {
            launch(Dispatchers.Default) {
                writeTask(writeOps)
            }
        }

        val readJobs = List(numReaders) {
            launch(Dispatchers.Default) {
                readTask(readIterations)
            }
        }


        writeJobs.joinAll()
        readJobs.joinAll()
    }

    val expectedWriteCount = numWriters * writeOps
    val finalSize = cowList.size

    println("\n------Results------")
    println("Total Execution Time: $totalTime ms")
    println("Initial List Size: $initialCount")
    println("[Write Task] Expected Total Write Ops : $expectedWriteCount")
    println("[Write Task] Actual Final Count: $finalSize (Initial Size + total Write Ops)")
    println("[Read Task] Read Tasks: $numReaders coroutines have done ${numReaders * readIterations} iterations without locks")
}