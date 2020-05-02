package coroutines

import coroutines.MyThreadFactory.threadFactory
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

fun main() {
    val tomcatExecutor = Executors.newFixedThreadPool(10, threadFactory("tomcat"))
    val appExecutor = Executors.newFixedThreadPool(10, threadFactory("app"))
    
    (1..100).map { i ->
        tomcatExecutor.submit {
            SessionHolder.set(Session("session $i from ${Thread.currentThread().name}"))
            val context =
                appExecutor.asCoroutineDispatcher().sessionAware(session = SessionHolder.get()!!)
            appLogic(context)
        }
    }.forEach { future: Future<*> -> future.get() }
    
    tomcatExecutor.shutdown()
    appExecutor.shutdown()
}

private fun appLogic(coroutineContext: CoroutineContext) {
    runBlocking(coroutineContext) {
        log("retrieving session ${SessionHolder.get()}")
    }
}

object MyThreadFactory {
    
    private val threadFactoryCount = AtomicInteger(1)
    
    fun threadFactory(name: String) = object : ThreadFactory {
        
        private val threadCount = AtomicInteger(1)
        private val factoryNumber = threadFactoryCount.getAndIncrement()
        
        override fun newThread(r: Runnable): Thread {
            return Thread(
                r,
                "pool-$name-${factoryNumber}-thread-${threadCount.getAndIncrement()}"
            )
        }
        
    }
    
}


data class Session(val id: String)

object SessionHolder {
    private var holder = ThreadLocal<Session?>()
    
    fun get(): Session? {
        return holder.get()
    }
    
    fun set(session: Session) {
        holder.set(session)
    }
    
    fun clear() {
        holder.set(null)
    }
}

fun ExecutorCoroutineDispatcher.sessionAware(
    session: Session
): ThreadLocalPreservingCoroutineDispatcher {
    return ThreadLocalPreservingCoroutineDispatcher(
        context = this,
        wrapperFactory = SessionThreadLocalWrapperFactory(session)
    )
}

class SessionThreadLocalWrapperFactory(private val session: Session) : ThreadLocalWrapperFactory {
    override fun <T> newThreadLocalWrapper(continuation: Continuation<T>): ThreadLocalWrapper<T> {
        return ContextThreadLocalWrapper(continuation, session)
    }
}

class ContextThreadLocalWrapper<T>(continuation: Continuation<T>, private val session: Session) :
    ThreadLocalWrapper<T>(continuation) {
    override fun resetThreadLocal() {
        SessionHolder.clear()
    }
    
    override fun propagateThreadLocal() {
        SessionHolder.set(session)
    }
}
