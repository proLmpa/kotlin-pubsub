package org.example.concurrent.Volatile

class ConfigManager {
    @Volatile
    var refreshInterval: Long = 1000L  // 밀리초 단위 리프레시 주기

    fun reloadConfig(newInterval: Long) {
        refreshInterval = newInterval
    }

    fun runTask() {
        while (true) {
            println("현재 주기: $refreshInterval ms")
            Thread.sleep(refreshInterval)
        }
    }
}

fun main() {
    val config = ConfigManager()

    Thread { config.runTask() }.start()

    Thread.sleep(2000)
    config.reloadConfig(500)  // 메인 메모리에 즉시 반영되어 다른 스레드가 바로 감지함
}