
import java.util.Scanner
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

data class Event(val message: String)

// Broker (Publisher & Queue 관리자)
object Broker {
    private val eventQueue: BlockingQueue<Event> = LinkedBlockingQueue<Event>()
    private val subscriberIdCounter = AtomicInteger(0)

    // Publisher가 메시지 발행 (put)
    fun publish(event : Event) {
        eventQueue.put(event)
        println("[Broker] Published: ${event.message}")
    }

    // Subscriber가 메시지 수령 (take)
    fun consume() : Event {
        return eventQueue.take()
    }

    fun getNewSubscriberId() : Int = subscriberIdCounter.incrementAndGet()
}

// Subscriber (메시지 소비자)
class Subscriber(private val id: Int, private val broker: Broker) : Runnable {

    override fun run() {
        println("[Subscriber-$id] started on ${Thread.currentThread().name}")

        try {
            while (!Thread.currentThread().isInterrupted) {
                val event = broker.consume()
                println("[Subscriber-$id] received: ${event.message}")
            }
        } catch (_: InterruptedException) {
            println("[Subscriber-$id] interrupted.")
            Thread.currentThread().interrupt()
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
    val reader = Scanner(System.`in`)

    while(true) {
        val count = try {
            reader.nextInt()
        } catch (_: Exception) {
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
                Broker.publish(Event("Message $i/$count"))
            }
            println("Published $count messages to the Broker.")
        }
    }
}
