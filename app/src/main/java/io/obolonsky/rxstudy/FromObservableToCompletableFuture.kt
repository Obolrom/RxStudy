@file:RequiresApi(Build.VERSION_CODES.N)
package io.obolonsky.rxstudy

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * [Observable] of type [T] to [CompletableFuture] of type [T]
 * Use this when you expect just a single item emitted from stream—for example,
 * when Rx wraps a method invocation or request/response pattern.
 * The [CompletableFuture] of type [T]
 * completes successfully when stream completes with exactly one
 * emitted value. Obviously, future completes with an exception when stream completed
 * in such a way or when it did not complete with exactly one item emitted.
 *
 * Typical [io.reactivex.Single]
 */
@SuppressLint("CheckResult")
fun <T> toCompletableFuture(observable: Observable<T>): CompletableFuture<T> {
    val completableFuture = CompletableFuture<T>()
    observable
        .singleOrError()
        .subscribe(
            completableFuture::complete,
            completableFuture::completeExceptionally
        )
    return completableFuture
}

fun <T> toCompletableFutureList(observable: Observable<T>): CompletableFuture<List<T>> {
    return toCompletableFuture(observable.toList().toObservable())
}

private fun singleJust() {
    val future: CompletableFuture<Int> = toCompletableFuture(Observable.just(1))

    future.thenApply {
        println("singleJust value: $it")
    }
}

private fun singleWithTimer() {
    val future: CompletableFuture<Int> = toCompletableFuture(
        Observable.timer(2, TimeUnit.SECONDS).map { 1 }
    )

    future.thenApply {
        println("singleWithTimer value: $it")
    }
}

private fun completableOfList() {
    val future: CompletableFuture<List<Int>> = toCompletableFutureList(
        Observable
            .timer(2, TimeUnit.SECONDS)
            .flatMap { Observable.just(1, 2, 3) }
    )

    future
        .thenApply { println("completableOfList value: $it") }
        .exceptionally { println("completableOfList error: $it") }
}

fun fromObservableToCompletableFutureDemo() {
//    singleJust()

//    singleWithTimer()

    completableOfList()
}