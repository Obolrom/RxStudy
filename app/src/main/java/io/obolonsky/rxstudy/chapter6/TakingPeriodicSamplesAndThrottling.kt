@file:SuppressLint("CheckResult")
package io.obolonsky.rxstudy.chapter6

import android.annotation.SuppressLint
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

private fun sampleTest() {
    val startTime = System.currentTimeMillis()
    Observable
        .interval(7, TimeUnit.MILLISECONDS)
        .timestamp()
        .sample(1, TimeUnit.SECONDS)
        .map { ts -> (ts.time() - startTime).toString() + " ms: " + ts.value() }
        .take(5)
        .subscribe(::println)
}

private fun sampleTest2() {
    val names = Observable
        .just("Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan", "Margaret", "Dorothy")

    val absoluteDelayMillis = Observable
        .just(0.1, 0.6, 0.9, 1.1, 3.3, 3.4, 3.5, 3.6, 4.4, 4.8)
        .map { d -> (d * 1_000).toLong() }

    val delayedNames = names
        .zipWith(absoluteDelayMillis) { n, d ->
            Observable
                .just(n)
                .delay(d, TimeUnit.MILLISECONDS)
        }
        .flatMap { o -> o }

    delayedNames
        .sample(1, TimeUnit.SECONDS)
        .subscribe(::println)
}

private fun sampleCustomObservable() {
    Observable
        .interval(12, TimeUnit.MILLISECONDS)
        .sample(
            Observable
                .just(1L, 2L, 3L)
                .flatMap {
                    Observable.just(it)
                        .delay(it, TimeUnit.SECONDS)
                }
        )
        .forEach { println(it) }
}

fun test() {
//    sampleTest()

//    sampleTest2()

    sampleCustomObservable()
}