
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class Message(val id: Int, val content: String)

object MessageBroker {
    private val messageMap = ConcurrentHashMap<Int, Message>()
    private val subscriberIdCounter = AtomicInteger(0)
    private val messageIdCounter = AtomicInteger(1)

    fun putMessage(content: String): Int {
        val newId = messageIdCounter.getAndIncrement()
        messageMap[newId] = Message(newId, content)
        return newId
    }

    fun getMessage(id: Int): Message? = messageMap[id]

    fun getLastMessageId(): Int = messageIdCounter.get()

    fun getNewSubscriberId() : Int = subscriberIdCounter.incrementAndGet()
}

class MessageSubscriber(private val id: Int, private val broker: MessageBroker) : Runnable {
    private var lastProcessedId: Int = 0

    override fun run() {
        println("[Subscriber-$id] started.")

        while(!Thread.currentThread().isInterrupted) {
            try {
                val lastMessageId = broker.getLastMessageId()

                for (i in lastProcessedId .. lastMessageId) {
                    val message = broker.getMessage(i)

                    if (message != null) {
                        println("[Subscriber-$id] Message received from Message-$i: ${message.content}")
                        lastProcessedId++
                    } else {
                        Thread.sleep(10)
                    }
                }

                if (lastMessageId == lastProcessedId) Thread.sleep(100)
            } catch (_ : InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }

        println("[Subscriber-$id] finished.")
    }
}

fun main() {
    val subscribers = 10
    val threads = mutableListOf<Thread>()

    repeat (subscribers) {
        val subscriberId = MessageBroker.getNewSubscriberId()
        val subscriberTask = MessageSubscriber(subscriberId, MessageBroker)

        val thread = Thread(subscriberTask, "Subscriber-$subscriberId")
        threads.add(thread)
        thread.start()
    }

    println("System started. Enter message count (0 to exit).")

    val reader = java.util.Scanner(System.`in`)

    while(true) {
        print(" > ")
        val count = try {
            if (reader.hasNextInt()) reader.nextInt() else {
                reader.nextLine()
                println("Invalid input. Please enter a valid number.")
                continue
            }
        } catch (_: Exception) {
            println("An error occurred during input.")
            reader.nextLine()
            continue
        }

        if (count == 0) {
            // 7. 0 입력 시 종료
            threads.forEach { it.interrupt() }
            threads.forEach { it.join() }

            println("All subscribers terminated. System exit.")
            break
        } else if (count > 0) {
            // 4. 메시지 발행
            for (i in 1..count) {
                MessageBroker.putMessage("Message $i/$count")
            }
            println("Published $count messages to the Broker.")
        }
    }
}