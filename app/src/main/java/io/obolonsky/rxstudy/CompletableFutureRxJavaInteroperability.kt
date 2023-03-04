@file:RequiresApi(Build.VERSION_CODES.N)
package io.obolonsky.rxstudy

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import java.util.concurrent.CompletableFuture

fun <T : Any> observe(completableFuture: CompletableFuture<T>): Observable<T> {
    return Observable.create { emitter ->
        completableFuture.whenComplete { value: T, exception: Throwable? ->
            if (exception != null) {
                emitter.onError(exception)
            } else {
                emitter.onNext(value)
                emitter.onComplete()
            }
        }

        // Don't do this!
        // This is a bad idea. We can create many Observables based on one CompletableFuture,
        // and every Observable can have multiple Subscribers. If just one Subscriber
        // decides to unsubscribe prior to Future’s completion, cancellation will affect all other
        // Subscribers

        // emitter.setCancellable { completableFuture.cancel(true) }
    }
}

fun rxFindById(id: Int): Observable<User> {
    return observe(findByIdAsync(id))
}

fun rxLocate(): Observable<GeoLocation> {
    return observe(locateAsync())
}

fun rxBook(flight: Flight): Observable<Ticket> {
    return observe(bookAsync(flight))
}

@SuppressLint("CheckResult")
fun completableFutureInteroperabilityDemo() {
    rxFindById(1)
        .zipWith(rxLocate()) { user, location ->
            user to location
        }
        .flatMap { (user, location) ->
            Observable.fromIterable(agencies)
                .flatMap { agency ->
                    observe(agency.searchAsync(user, location))
                }
        }
        .firstElement()
        .flatMapObservable { rxBook(it) }
        .subscribe(
            {
                println("rx ticket: $it")
            },
            {
                println("rx error: $it")
            },
            {
                println("rx complete")
            }
        )
}