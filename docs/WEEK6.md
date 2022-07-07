# Week6

## 선점형 멀티태스킹

대부분의 OS 는 선점형 멀티태스킹 방식을 취하고 있음. 예를 들면 A 프로세스나 스레드를 스케쥴링 하다가도, B 프로스세스나 스케쥴러가 좀 더 높은 우선순위로 실행되야 한다면, 
OS 가 A 프로세스를 중단시키고, B 프로세스를 실행시킬 수 있음

## 비선점형 멀티태스킹

OS 가 강제로 현재 실행중인 프로그램을 멈출 수 없음. 로드된 프로그램이 종료되어야 다른 프로그램이 실행됨. 보통 경량스레드들이 이에 속함. 보통 하나의 로직이 죽으면 전부 다 죽음.


### 비선점형 멀티태스킹의 단점

단점은 진짜 동시성이 아니다. 하나의 작업을 여러개의 쓰레드나 프로세서로 분산시킬 수 없음.
-> 각 작업을 길게 쪼개면 각각 조각을 스레드로 분산시킬 수는 있음.

<img width="1000" alt="image" src="https://user-images.githubusercontent.com/57784077/177766375-e63f1452-d92d-4e53-bc82-ece0ee749950.png">

위의 사진 처럼 하나의 Job 을 잘게 쪼개서 마치 동시에 실행되는 것처럼 보여줌. **Coroutine 도 label 을 붙이면서 label 마다 suspend** 될 수 있는데, 그게 요거랑 같은건가보다. 이 생각을 했다. (추측임. 아닐수도). 

### 비선점형 멀티태스킹의 장점

장점또한 마찬가지로 진짜 동시성이 아니다. 동기화 문제가 일어나지 않게 할 수 있음 (일어나게도 할 수 있음). JavaScript 는 SingleThread 가 아니라, Process 를 처리할때 비선점형으로 처리하는 Main Process 가 하나밖에 없기에 Single Thread 라고 하는 것임.

### 구현체

OS 레벨 - 파이버
SW 레벨 - 그린스레드, 코루틴, 제네레이터 등

### 코틀린에서는?

sequence, coroutine 을 통해 소프트웨어 스택으로 구현되어 있음. 

### JAva 에서는?

- 그린스레드
- Project loom : OS Level 의 파이버와 연동

## CPS (Continuation Passing Style)

Continuation 이란 어떤 Thread 가 Coroutine 에 들어갔다가 나와도, 그 Coroutine 안의 정보들은 Persistence 하게 보존하는 Context

코루틴의 일반적인 구현(함수형 CPS가 원형)
- 루틴: 진입하면 반드시 반환까지 한 번에 실행됨.
- 코루틴: 진입한 뒤 **중간에 반환하고 다시 그 위치 부터 재실행** 가능 (suspend func)

보통 중간 반한되는 포인트마다 묶어서 서브루틴으로 만들어 서브루틴의 배열화 하면 재진입 시 다음 서브루틴을 실행하는 방식으로 처리. 보통 나누는 기준은 
yield, await 등 으로 서브루틴을 나눔. 코틀린은 suspend 함수를 호출하는 기준으로 나뉜다.

# Linked Task

위의 말을 들어봤을때 하나의 Job 을 세분화 하고, 그걸 마치 동시성으로 실행되는 것처럼 보일 수 있다는 뜻은 결국 Sequential 하게 일어난다는 것인데, 이를 표현하려면 어떻게 해야할까?
아마 SubTask 들이 연결된 LinkedTask 로 표현하면 좋을 것이다.

```kotlin
class Task(
    val run: (Controller) -> Unit
) {
    var isCompleted = false
    var result: Result<Any?>? = null
    var next: Task? = null
}


class Controller internal constructor(
    private val task: Task
) {
    val data get() = task.result

    fun cancel(throwable: Throwable) {
        task.next?.result = Result.failure(throwable)
        task.isCompleted = true
    }

    fun resume(data: Any? = null) {
        task.next?.result = Result.success(data)
        task.isCompleted = true
    }
}
```

위의 코드를 보면 resume 이나 cancel 모두 task 는 종료되고, 다음 Task 로 이동되게 된다. 따라서 resume / cancel 모두 next.result 에 현재의 데이터를 넘겨주게 된다.

<img width="200" alt="image" src="https://user-images.githubusercontent.com/57784077/177770785-cc5a1347-03dd-4e59-bae8-f071a3565533.png">

## EventLooper

```kotlin
class EventLooper(
    private val dispatcher: Dispatcher
): Runnable {
    private val tasks: Queue<Task> = LinkedList()
    private val currTask: Task? = null

    fun linkedTask(vararg blocks: (Controller) -> Unit) {
        if (blocks.isEmpty()) return
        synchronized(tasks) { // multi-Thread 에서 실행될 수 있으므로.
            var prev = Task(blocks[0])
            tasks.add(prev) // Add Task to TaskQueue
            for (i in 1..blocks.lastIndex) { // Linked Task
                val task = Task(blocks[i])
                prev.next = task
                prev = task
            }
        }
    }
}
```

일단 Thread 를 어떻게 운영할것인지는 Dispatcher 에게 Runnable Interface 를 구현하여 일임하고, EventLooper 는 TaskQueue 를 받아서 Queue 에서 마지막 작업을 빼내서 currentTask 로 사용하는 구조.

어려워 보이지만, 잘 들여다보면 여러개의 Task Block 을 Linking 하여 결국 Queue 에는 하나의 Task 로 들어간다. SubRoutine + SubRoutine = Routine 요런식이라고 생각하면 될듯하다.

```kotlin
    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            Thread.sleep(16) // Thread Block 을 막기 위해서
            synchronized(this) { // Task 는 Synchronized 대상
                if (currTask != null) { // 실행중이라는 뜻.
                    currTask?.let {curr ->
                        if (curr.isCompleted) { // 현재 Task 가 끝났는지 확인
                            curr.next?.let { tasks.add(it) } // 현재 Task 가 끝났다면 다음 Task 를 Queue 에 집어 넣음.
                            currTask = null
                        }
                    }
                } else {
                    tasks.poll()?.let {
                        currTask = it
                        it.run(Controller(it))
                    }
                }
            }
        }

    }
```

## Dispatcher

```kotlin
interface Dispatcher {
    fun start(looper: EventLooper)
    fun join()
}

class FixedDispatcher(
    private val threads: Int
) : Dispatcher {
    private val executor = Executors.newFixedThreadPool(threads)

    override fun start(looper: EventLooper) {
        for (i in 1..threads) {
            executor.execute(looper)
        }
    }

    override fun join() {
        while (!executor.isShutdown) {}
    }
}
```

FixedDispatcher 는 Thread 가 정해진 Pool 을 만들고, 내부에서 Looper 를 스레드 갯수만큼 돌면서 실행시킴. Looper 의 작업을 여러 스레드가 처리함. 여기서 약간 코루틴의 내부 구조가 잘 이해가 갔는데, 사실 Coroutine 의 suspend function 을 여러 스레드가 Heap 에 있는 Context 를 공유하며 실행시킨 다는 것은 알았는데, 약간 이렇게 코드를 보니 더 새로웠다. 

이걸 보면서 느낀게 내가 Dispatcher 를 고르면 그 Dispatcher 가 내 Job 을 실행시킨 다는게 결국 그 Dispatcher 의 Thread 들이 EventLooper 를 돌면서 내 Job 을 실행시키는 거구나. 이런 생각이 들었다.

## 실제 실행

```kotlin
fun main() {
    val looper = EventLooper(FixedDispatcher(10))

    for (i in 0..5) {
        looper.linkedTask({
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        })
    }

    looper.launch()
    looper.join()
}

/**
 * Executed Result
0-0 24
1-0 18
2-0 23
3-0 21
4-0 16
5-0 17
0-0 22
1-0 18
2-0 24
3-0 18
4-0 22
5-0 23
0-0 15
1-0 17
2-0 18
3-0 20
4-0 19
5-0 22
 */
```

위의 Thread Id 를 보면 각각 다른 Thread 들이 실행하거나 또는 같은 Thread 가 실행했음을 알 수 있음. 즉, 하나의 큰 Task 를 여러 Thread 들이 실행가능함. 위에 Thread 가 달라서 Multi-Thread 에서 동시에 실행되는 것처럼 보이지만, 결국 EventQueue 에 쌓인 Task 순서대로 실행됨을 알 수가 있다.

## Serial Task

Task 하 

```kotlin
/**
 * Ready 는 딱 생성됬을때 상태
 * Mark 는 사용자 요구 사항 마킹 (Confirm 되지 않으면 의미 없음)
 * Confirm 사용자의 요구 사항 마킹 수용
 */
enum class Stat {
    READY, MARK, CONFIRM
}

class Task internal constructor(
    val run: (Controller) -> Unit
) {
    internal var isStarted = Stat.READY
    internal var isCompleted = Stat.READY
    internal var result: Result<Any?>? = null
    internal var next: Task? = null
}
```

이제는 시작했는지 안했는지 구분값도 설정해줌.

```kotlin
class Controller internal constructor(
    private val task: Task
) {
    val data get() = task.result

    fun cancel(throwable: Throwable) {
        task.next?.result = Result.failure(throwable)
        task.isCompleted = Stat.MARK
    }

    fun resume(data: Any? = null) {
        task.next?.result = Result.success(data)
        task.isCompleted = Stat.MARK
    }
}
```

MARK 를 하는 이유는 내가 이걸 할꺼야~ 라고 마킹한다고 생각하면 된다. 결국 Thread 가 이를 용인하고 실행시켜준 뒤 Confirm 으로 바꿔주는 구조이다.

```kotlin
class SerialTask(
    private val dispatcher: Dispatcher,
    vararg blocks: (Controller) -> Unit
) : Runnable {
    private val task: Task

    /**
     * 생성자에서 Block 으로 Task 를 구성해버림. linkedTask 가 오직 SerialTask 가 실행되는 시점에만 사용됨.
     */
    init {
        if (blocks.isEmpty()) throw Throwable("no blocks")
        var prev = Task(blocks[0])
        task = prev
        prev.isStarted = Stat.MARK
        for (i in 1..blocks.lastIndex) {
            val task = Task(blocks[i])
            prev.next = task
            prev = task
        }
    }

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            Thread.sleep(5)
            if (task.isCompleted == Stat.MARK) {
                task.next?.let {
                    it.isStarted = Stat.MARK
                    task = it
                }
            }
            if (task.isStarted == Stat.MARK) {
                task.run(Controller(task))
                task.isStarted = Stat.CONFIRM
            }
        }
    }

    fun launch() {
        dispatcher.start(this)
    }
}
```

이 Class 의 목적은 Serial 된 Task 를 만들기 위함이다. TaskQueue 가 필요없다.
Maeng 교수님 강의를 따라가면 코드를 바꿔야 하는데, 나는 기존코드도 살리고 확장성도 좀 더 더해주고 싶어서 일단 코드를 아래처럼 바꿨다.

```kotlin
interface EventLooper : Runnable {
    fun launch()
}

class SerialTask(
    private val dispatcher: Dispatcher,
    vararg blocks: (Controller) -> Unit
) : EventLooper {
    private var task: Task

    // 생략..

    override fun launch() {
        dispatcher.start(this)
    }
}
```

```kotlin
fun main() {
    val dispatcher = FixedDispatcher(10)

    for (i in 0..5) {
        val looper = SerialTask(dispatcher, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        }, {
            println("$i-0 ${Thread.currentThread().id}")
            Thread.sleep(i * 100L)
            it.resume()
        })
        looper.launch()
    }
}

/*
4-0 19
0-0 15
2-0 17
5-0 20
1-0 16
3-0 18
0-1 15
0-2 15
1-1 16
2-1 17
1-2 16
3-1 18
4-1 19
2-2 17
5-1 20
3-2 18
4-2 19
5-2 20
*/
```

이렇게 Serial Task 로 실행하면 장점은 무엇일까? Result 를 보면 알 수 있듯이, for 문을 돌면서 dispatcher 의 Thread 로 한번의 여러 가지 Serial Task 를 처리하게 되는 것이다. 이게 잘보면 **하나의 SerialTask 는 동일한 Thread 로 실행**된다. 이렇게 처리하게 되면 아까전 처럼 A -> B -> C 이런식으로 Sequencial 하게 처리되는게 아니라, 모든 Thread 들 사이에서 경쟁되며 사용될 수 있다. **좀 더 Thread 에서 효율적**이다. **synchronize 또한 없음을 확인**할 수 있는데, 그 이유는 위에서 설명한대로 하나의 SerialTask 는 하나의 Thread 로 처리되기 때문이다.

이게 비선점형 멀티태스킹 방식의 장점인데 위 코드처럼 synchronize 를 할 필요가 없다. 다만 위의 코드는 SerialJob 간의 순서는 보장되지 않는다.

## Continuation

```kotlin
class ContinuationTask(
    private val dispatcher: Dispatcher,
    isLazy: Boolean,
    block: (Controller) -> Unit
) : EventLooper {
    private val task: Task = Task(block)

    init {
        if (!isLazy) launch()
    }

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            Thread.sleep(5)
            if (task.isCompleted == Stat.MARK) break
            if (task.isStarted == Stat.READY) {
                task.isStarted == Stat.CONFIRM
                task.run(task.continuation)
            }
            task.continuation.failed?.let {throw it}
        }
    }

    override fun launch() {
        dispatcher.start(this)
    }
}

fun main() {
    val dispatcher = FixedDispatcher(10)

    for (i in 0..5) {
        ContinuationTask(dispatcher, false) {
            when (it.step) {
                0 -> {
                    println("$i-0 ${Thread.currentThread().id}")
                    it.resume(1)
                }
                1 -> {
                    println("$i-0 ${Thread.currentThread().id}")
                    it.resume(2)
                }
                2 -> {
                    println("$i-0 ${Thread.currentThread().id}")
                    it.complete()
                }
            }
        }
    }
    dispatcher.join()
}
```

위의 코드를 보면 step(Coroutine 에서는 label) 별로 나누어져있는데, 이는 초반에 설명했듯이, 컴파일러가 yield() 나 suspend 등을 만나면 label 별로 나눠버리는 것이다. 이래서 suspend 가능한것이다. 원하는 step 으로 jump 뛸 수 있기에. 결국 원하는 subroutine 으로 jump 할 수 있다는 것이다. (코루틴도 Java Code 를 확인해보면 위와 같다.) 바로 이러한 스타일이 CPS 이다.

```kotlin
class ContinuationTask internal constructor(
    val run: (Continuation) -> Unit
) {
    internal var isStarted = Stat.READY
    internal var isCompleted = Stat.READY
    internal var continuation = Continuation(this)
    internal var env: MutableMap<String, Any?> = mutableMapOf()
}

class Continuation internal constructor(
    private val task: ContinuationTask,
) {
    var step = 0
        private set

    operator fun get(key: String): Any? = task.env[key]
    operator fun set(key: String, value: Any?) { task.env[key] = value }
    internal var failed: Throwable? = null

    fun cancel(throwable: Throwable) {
        failed = Throwable("step: $step, env: ${task.env}", throwable)
        task.isCompleted = Stat.MARK
    }

    fun complete() {
        task.isCompleted = Stat.MARK
    }

    fun resume(step: Int) {
        this.step = step
        task.isStarted = Stat.READY
    }
}
```

위의 코드를 보면 결국 나갔다 들어와도 상태가 저장되어 있을 수 있는 이유는 **env** 에 local variable 등을 저장해둘 수 있기 때문이다. **다만 이 영역은 Heap 에 저장**된다. 보통의 Thread 는 자신의 Stack 변수에 보통 local variable 을 담아두고 Context Switching 이 일어날때 이를 전달해줘야 한다. 하지만, 지금과 같은 경우는 Heap 에 저장되므로 Context Switching 비용이 적다. (없다고 해야하나?)

## 강의를 들으면서 한 생각

### isCanceled 와 예외처리를 만들면 이렇게 하면 되지 않을까?

그냥 위의 코드를 보면서 느낀건데 코루틴 코드상에서 isCanceled 상태를 만들고, UnCaughtException 이 발생하면 해당 Task 를 isCanceled = true 로 만들고, async 의 경우에는 await() 시점에 isCanceled 면 예외를 rethrow 하고, launch 의 경우에는 isCanceled 로 만듬과 동시에 rethrow 하고 있지 않을까? 라는 생각을 했다.

## 후기

평소에 CS 공부를 꾸준하게 해두기 잘했단 생각이 많이 들었다. 코루틴의 내부 구조가 어떻게 돌아갔는지 코드로 조금 정리해서 좀 더 깊게 이해하고, 대충 이렇게 돌겠네.. 라고 이해할 수 있었다. 읽고 있는 코루틴 책도 한번 더읽고, 다음 코드스피츠 코루틴 강좌는 꼭 신청해봐야겠다는 생각을 했다.

