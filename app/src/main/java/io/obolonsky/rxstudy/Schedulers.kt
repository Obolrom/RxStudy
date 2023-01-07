package io.obolonsky.rxstudy

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.internal.schedulers.SchedulerWhen
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

var start = System.currentTimeMillis()

// call me
fun schedulersTest() {
    example1()

    whenScheduler()
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