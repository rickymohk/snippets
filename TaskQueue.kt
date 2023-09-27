import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resumeWithException


class TaskQueue(private val tag:String? = null) : java.io.Closeable{

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val logTag = tag ?: this.toString()

    private open class Task(val tag:String?)
    private class SuspendTask(tag:String?, val block: suspend () -> Any?, val continuation: Continuation<Any?>?) : Task(tag)
    private class ChannelFlowTask(tag:String?,val capacity: Int, val block: suspend ProducerScope<Any>.() -> Unit, val producerScope: ProducerScope<Any>) : Task(tag)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val blocks = Channel<Task>().apply {
        Log.d(tag ?: logTag,"Launching coroutine")
        var ongoingTask: Task? = null
        invokeOnClose {
            val cancelTask : (Task)->Unit = {task ->
                if (task is SuspendTask)
                {
                    task.continuation?.resumeWithException(it ?: CancellationException())
                }
                else if( task is ChannelFlowTask)
                {
                    task.producerScope.close(it)
                }
            }
            Log.d(logTag,"closed. Clean up ongoingTask $ongoingTask")
            ongoingTask?.also(cancelTask)
            Log.d(logTag,"closed. Clean up pending tasks")
            var res = tryReceive()
            while(res.isSuccess)
            {
                Log.d(logTag,"closed. Clean up pending task $res")
                res.getOrNull()?.also(cancelTask)
                res = tryReceive()
            }
            Log.d(logTag,"closed. Clean up done")
        }
        coroutineScope.launch(Dispatchers.Default)
        {
            Log.d(logTag,"start")
            while (coroutineScope.isActive){
//                Log.d(logTag,"receiving new block")
                val task = receive().also { ongoingTask = it }
                if(task is SuspendTask){
//                Log.d(logTag,"new block received (${task.tag})")
                    val result = task.block()
//                Log.d(logTag,"new block executed (${task.tag})")
                    if(!coroutineScope.isActive || isClosedForSend) break
                    task.continuation?.resume(result)
//                Log.d(logTag,"new block resumed (${task.tag})")
                }
                else if(task is ChannelFlowTask)
                {
                    Log.d(tag,"dispatchChannelFlow $tag start produce")
                    try {
                        for(value in produce<Any>(block = task.block, capacity = task.capacity))
                        {
                            if(!coroutineScope.isActive || isClosedForSend) break
                            task.producerScope.send(value)
                        }
                        task.producerScope.close()
                    }
                    catch (ex:Throwable)
                    {
                        Log.e(tag,"dispatchChannelFlow $tag error",ex)
                        task.producerScope.close(ex)
                    }
                }
                ongoingTask = null
            }
            Log.d(logTag,"end")
        }
    }

    private fun addBlock(tag: String? = null, block:suspend ()->Any?,continuation: Continuation<Any?>?)
    {
//        Log.d(logTag,"add block (${tag})")
        coroutineScope.launch(Dispatchers.Default)
        {
            blocks.send(SuspendTask(tag,block,continuation))
        }
    }

    suspend fun dispatchSuspend(tag:String? = null, block: suspend () -> Unit) = blocks.send(SuspendTask(tag,block,null))

    fun dispatch(tag:String? = null, block: suspend () -> Unit){
        addBlock(tag, block,null)
    }

    suspend fun<T> dispatchAndWait(tag:String? = null,block: suspend ()->T) : T = suspendCancellableCoroutine { continuation ->
        addBlock(tag,block, continuation as Continuation<Any?>)
    }

    fun<T> dispatchChannelFlow(tag:String? = null,capacity:Int = 0, block: suspend ProducerScope<T>.()->Unit) = channelFlow<T> {
        Log.d(tag,"dispatchChannelFlow $tag")
        blocks.send(ChannelFlowTask(tag,capacity, block as (suspend ProducerScope<Any>.()->Unit) ,this as ProducerScope<Any>))
        awaitClose {
            Log.d(tag,"dispatchChannelFlow $tag closed")
        }
    }

    override fun close() {
        coroutineScope.takeIf { it.isActive }?.cancel()
        blocks.close(CancellationException())
    }
}
