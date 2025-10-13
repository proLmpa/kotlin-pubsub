package org.example.concurrent.Volatile

import java.util.concurrent.atomic.AtomicBoolean

class MyClass {
    @JvmField var years: Int = 0
    @JvmField var months: Int = 0

    @Volatile @JvmField var days: Int = 0

    @Synchronized
    fun update(years: Int, months: Int, days: Int) {
        this.years = years
        this.months = months
        this.days = days // (1) volatile 변수 쓰기 : 가시성 보장 및 명령 재배치 방지
    }

    @Synchronized
    fun readSnapshot(): Triple<Int, Int, Int> {
        val d = this.days // (2) volatile 변수 읽기 : 이후 일반 변수 읽기도 최신 값 보장
        val m = this.months
        val y = this.years

        return Triple(y, m, d)
    }
}

fun main() {
    val myClass: MyClass = MyClass()
    val maxWrites = 10_000_000
    val verificationFailed = AtomicBoolean(false)

    val writerThread = Thread {
        for (i in 1..maxWrites) {
            myClass.update(i,i,i)
            if(i % 1000 == 0) Thread.yield()
        }
    }

    val readerThread = Thread {
        while(myClass.days < maxWrites && !verificationFailed.get()) {
            val (y, m, d) = myClass.readSnapshot()

            if (d > 0) {
                if (d != y || d != m) {
                    println("Visibility Failed.")
                    println("Days: $d, Months: $m, Years: $y")
                    verificationFailed.set(true)
                    break
                }
            }
        }

        if (!verificationFailed.get()) {
            println("Visibility Success.")
        }
    }

    writerThread.start()
    readerThread.start()

    writerThread.join()
    readerThread.join()

    println("Verification result : ${if(verificationFailed.get()) "FAILED" else "SUCCESS"}")
}