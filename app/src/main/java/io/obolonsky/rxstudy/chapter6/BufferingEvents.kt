@file:SuppressLint("CheckResult")
@file:RequiresApi(Build.VERSION_CODES.O)
package io.obolonsky.rxstudy.chapter6

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private fun bufferWithSize() {
    Observable
        .range(1, 8)
        .buffer(3) // <- try to comment this operator
        .forEach(::println)
}

private fun bufferWithSizeWindowed() {
    Observable
        .range(1, 8)
        .buffer(3, 2) // try to change 'skip' param
        .forEach(::println)
}

private fun bufferByTimePeriod() {
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
        .buffer(1, TimeUnit.SECONDS)
        .subscribe(::println)
}

// see page 218 of book
private fun bufferBusinessCase() {
    val businessStart = LocalTime.of(9, 0)
    val businessEnd = LocalTime.of(17, 0)

    fun isBusinessHour(): Boolean {
        val zone = ZoneId.of("Europe/Warsaw")
        val zdt = ZonedDateTime.now(zone)
        val localTime = zdt.toLocalTime()
        return !localTime.isBefore(businessStart)
                && !localTime.isAfter(businessEnd);
    }

    val insideBusinessHours = Observable
        .interval(1, TimeUnit.SECONDS)
        .filter { isBusinessHour() }
        .map { Duration.ofMillis(100) }

    val outsideBusinessHours: Observable<Duration> = Observable
        .interval(5, TimeUnit.SECONDS)
        .filter { !isBusinessHour() }
        .map { Duration.ofMillis(200) }

    val openings = Observable.merge(insideBusinessHours, outsideBusinessHours)

    Observable
        .interval(27, TimeUnit.MILLISECONDS)
        .buffer(openings) { duration ->
            Observable.empty<Int>()
                .delay(duration.toMillis(), TimeUnit.MILLISECONDS)
        }
        .forEach(::println)

}

fun testBufferingEvents() {
//    bufferWithSize()

//    bufferWithSizeWindowed()

//    bufferByTimePeriod()

    bufferBusinessCase()
}