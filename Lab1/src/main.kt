import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlin.coroutines.coroutineContext

suspend fun main(): Unit = coroutineScope {
    val flow = MutableSharedFlow<Int>(
        replay = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    launch {
        flow
            .takeWhile { it != -1 }
            .collect { println("Job A got: $it") }
    }
    launch {
        flow
            .takeWhile { it != -1 }
            .collect { println("Job B got: $it") }
    }

    (1..3).forEach {
        delay(500)
        flow.emit(it)
    }

    delay(1000)
    launch {
        flow
            .takeWhile { it != -1 }
            .collect { println("Job C got: $it") }
    }

    flow.emit(-1)
    delay(1000)


    val stateFlow = MutableStateFlow<State>(State.Loading)

    launch {
        stateFlow
            .takeWhile { it != State.Disconnect }
            .collect {
                when (it) {
                    is State.Loading -> println("loading...")
                    is State.Success -> println("current state: ${it.data}")
                    is State.Failure -> println("error: ${it.error.message}")
                    is State.Disconnect -> {}
                }
            }
    }

    delay(2000)
    stateFlow.value = State.Failure(Exception("error getting state"))

    delay(1000)
    stateFlow.value = State.Loading

    delay(2000)
    stateFlow.value = State.Success(10)

    delay(1000)
    stateFlow.value = State.Disconnect
}

sealed class State {
    data object Loading: State()
    data class Success(val data: Int): State()
    data class Failure(val error: Throwable): State()
    data object Disconnect: State()
}