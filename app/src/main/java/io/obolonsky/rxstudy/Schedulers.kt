package io.obolonsky.rxstudy

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.internal.schedulers.SchedulerWhen
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


var start = System.currentTimeMillis()

// call me
fun schedulersTest() {
//    example1()

//    whenScheduler()

//    brokenParallelism()

//    trueParallelism()

//    parallelismWithGroupBy()

//    observeOnTest()

//    observeOnWithLatencyOfStreamTransforming()

    checkingDelayThings()
}

fun example1() {
    val scheduler = Schedulers.trampoline()
    val worker = scheduler.createWorker()

    log("Main start")
    worker.schedule {
        log(" Outer start")
        Thread.sleep(TimeUnit.SECONDS.toMillis(2))
        worker.schedule {
            log(" Inner start")
            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
            log(" Inner end")
        }
        log(" Outer end")
    }
    log("Main end")
    worker.dispose()
}

fun log(label: Any) {
    println(
        (System.currentTimeMillis() - start).toString() + "\t| " +
                Thread.currentThread().name + "\t| " +
                label
    )
}

fun whenScheduler() {
/*
Limit the amount concurrency two at a time without creating a new fix size thread pool:
       Scheduler limitScheduler = Schedulers.computation().when(workers -> {
            // use merge max concurrent to limit the number of concurrent
            // callbacks two at a time
            return Completable.merge(Flowable.merge(workers), 2);
       });
*/

    Schedulers.computation().`when`<SchedulerWhen> { workers ->
        Completable.merge(Flowable.merge(workers), Runtime.getRuntime().availableProcessors() / 2)
    }
}

// sucks
fun brokenParallelism() {
    val schedulerA = Schedulers.io()
    val rxGroceries = RxGroceries()

    val totalPrice = Observable
        .just("bread", "butter", "milk", "tomato", "cheese")
        .subscribeOn(schedulerA) //BROKEN!!!
        .map { prod -> rxGroceries.doPurchase(prod, 1) }
        .reduce(BigDecimal::add)
        .toSingle()
        .subscribe(
            {
                println("output: $it")
            },
            {
                println("error: $it")
            }
        )
}

// cool
fun trueParallelism() {
    val schedulerA = Schedulers.io()
    val rxGroceries = RxGroceries()

    val totalPrice = Observable
        .just("bread", "butter", "milk", "tomato", "cheese")
        .subscribeOn(schedulerA) // can be removed, emissions are low cost
        .flatMap { prod ->
            rxGroceries.purchase(prod, 1)
                .subscribeOn(schedulerA)
        }
        .reduce(BigDecimal::add)
        .toSingle()
        .subscribe(
            {
                println("output: $it")
            },
            {
                println("error: $it")
            }
        )
}

// cool
fun parallelismWithGroupBy() {
    val schedulerA = Schedulers.io()
    val rxGroceries = RxGroceries()

    val totalPrice = Observable
        .just("bread", "butter", "egg", "milk", "tomato",
            "cheese", "tomato", "egg", "egg")
        .groupBy { prod -> prod }
        .flatMapSingle { prod ->
            prod
                .count()
                .map { quantity -> prod.key to quantity }
        }
        .flatMap { (key, quantity) ->
            rxGroceries.purchase(key!!, quantity.toInt())
                .subscribeOn(schedulerA)
        }
        .reduce(BigDecimal::add)
        .toSingle()
        .subscribe(
            {
                println("output: $it")
            },
            {
                println("error: $it")
            }
        )
}

// just to don't forget
fun observeOnTest() {
    val schedulerA = Schedulers.io()
    val schedulerB = Schedulers.computation()
    val schedulerC = Schedulers.from(Executors.newFixedThreadPool(4))

    log("Starting")
    val obs = Observable.just(1, 2, 3)
    log("Created")
    val test = obs
        .doOnNext { x -> log("Found 1: $x") }
        .observeOn(schedulerB)
        .doOnNext { x -> log("Found 2: $x") }
        .observeOn(schedulerC)
        .doOnNext { x -> log("Found 3: $x") }
        .subscribeOn(schedulerA)
        .subscribe(
            { x -> log("Got 1: $x") },
            { error -> error.printStackTrace() }
        ) { log("Completed") }
    log("Exiting")
}

// take a look at buffering at some stages
fun observeOnWithLatencyOfStreamTransforming() {
    val schedulerA = Schedulers.io()
    val schedulerB = Schedulers.computation()
    val schedulerC = Schedulers.from(Executors.newFixedThreadPool(4))

    log("Starting")
    val obs = Observable.just(1, 2, 3)
    log("Created")
    val test = obs
        .doOnNext { x -> log("Found 1: $x") }
        .observeOn(schedulerB)
        .doOnNext { x ->
            Thread.sleep(10)
            log("Found 2: $x")
        }
        .observeOn(schedulerC)
        .doOnNext { x -> log("Found 3: $x") }
        .subscribeOn(schedulerA)
        .subscribe(
            { x -> log("Got 1: $x") },
            { error -> error.printStackTrace() }
        ) { log("Completed") }
    log("Exiting")
}

// experiments :)
@SuppressLint("CheckResult")
fun checkingDelayThings() {
    val scheduler = Schedulers.from(Executors.newFixedThreadPool(1))
    val items = listOf("bread", "butter", "egg")

    fun runWithDelay(item: String, scheduler: Scheduler) {
        Observable.just(item)
            .delay(5, TimeUnit.SECONDS, scheduler)
            .subscribe { println("runWithDelay: $it") }
    }

    println("before...")

    items.forEach { item -> runWithDelay(item, scheduler)}

    Observable.just(1)
        .subscribeOn(scheduler)
        .subscribe { println("is this magic?") }

    println("started...")
}

internal class RxGroceries {
    fun purchase(productName: String, quantity: Int): Observable<BigDecimal> {
        return Observable.fromCallable { doPurchase(productName, quantity) }
    }

    fun doPurchase(productName: String, quantity: Int): BigDecimal {
        log("Purchasing $quantity $productName")
        //real logic here
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        log("Done $quantity $productName")
        return quantity.toBigDecimal()
    }
}