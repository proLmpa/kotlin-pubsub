package org.example.concurrent

/*
1. Subscriber를 멀티쓰래드로 구현. 10개 정도를 실행 한다.
  각각의 Subscriber는 고유의 ID를 가진다
2. Subscriber 가 Broker 에게 요청하여 데이터를 가져 가는 형태로 구현한다
3. Broker 를 Singleton 으로 구현
4. 0 이 아닌 숫자를 입력하면 메시지를 개수만큼 발행.
5. 발행 된 메시지는 SubScriber 가 Broker 에게서 받아간다.
   SubScriber 는 받은 메시지와 SubScriber ID 를 출력한다
6. 0 이 아닌 숫자를 입력할경우 메세지 발행 후 다시 숫자를 입력 받음(무한 반복)
7. 0 를 입력하면 종료
*/

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

data class Event(val message: String)

// 3. Singleton Broker
object Broker {
    private val messageQueue = LinkedBlockingQueue<Event>()
    private val subscriberIdCounter = AtomicInteger(0)

    // 4. Message Publishing..
    fun putEvent(event : Event) {
        messageQueue.add(event)
    }

    // 2, 5. The Subscriber calls Broker and takes message
    fun getEvent() : Event {
        return messageQueue.take()
    }

    fun getNewSubscriberId() : Int {
        return subscriberIdCounter.incrementAndGet()
    }
}

// 1, 5. Multi-threading Subscribers
class Subscriber(private val id: Int, private val broker: Broker) : Runnable {

    override fun run() {
        println("[Subscriber-$id] started.")

        while(!Thread.currentThread().isInterrupted) {
            try {
                val event = broker.getEvent()
                println("[Subscriber-$id] received message: ${event.message}")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }

        println("[Subscriber-$id] terminated.")
    }
}

fun main() {
    val numberOfSubscribers = 10
    val threads = mutableListOf<Thread>()

    // 1. Multi-threading Subscribers
    repeat (numberOfSubscribers) {
        val subscriberId = Broker.getNewSubscriberId()
        val subscriberTask = Subscriber(subscriberId, Broker)

        val thread = Thread(subscriberTask, "Subscriber-$subscriberId")
        threads.add(thread)
        thread.start()
    }

    println("System started.")
    print(" > ")
    val reader = java.util.Scanner(System.`in`)

    while(true) {
        val count = try {
            reader.nextInt()
        } catch (e: Exception) {
            println("Invalid input. Please enter a valid number")
            reader.nextLine()
            continue
        }

        if (count == 0) {
            // 7. If 0, exit.
            threads.forEach { it.interrupt() }
            threads.forEach { it.join() }

            println("All subscribers termintated. System exit.")
            break
        } else if (count > 0) {
            // 4. Else, publish message
            for (i in 1..count) {
                Broker.putEvent(Event("Message $i/$count"))
            }
            println("Published $count messages to the Broker.")
        }
    }
}
