package org.example.concurrent.Volatile

@Volatile private var ready = false

fun main() {
    val wait = Thread {
        println("[Thread-1] Wait: waiting for ready signal...")
        while (!ready) {
            // busy-wait (Spin-Wait)
            Thread.yield()
        }
        println("[Thread-1] Wait: detected ready=true, start task")
    }

    val signal = Thread {
        Thread.sleep(500)
        ready = true
        println("[Thread-2] Signal: ready set to true")
    }

    wait.start()
    signal.start()
    wait.join()
    signal.join()
}