package io.obolonsky.rxstudy

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.concurrent.TimeUnit


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RxJavaUnitTesting {

    @Test
    fun `first rx java test from book`() {
        val list = Observable
            .range(1, 3)
            .concatMap { x -> Observable.just(x, -x) }
            .map(Any::toString)
            .toList()
            .blockingGet()

        assertEquals(listOf("1", "-1", "2", "-2", "3", "-3"), list)
    }

    @Test
    fun `first test modified`() {
        Observable
            .range(1, 3)
            .concatMap { x -> Observable.just(x, -x) }
            .map(Any::toString)
            .toList()
            .test()
            .assertValue(listOf("1", "-1", "2", "-2", "3", "-3"))
    }

    @Test
    fun `failure test`() {
        Observable
            .range(1, 3)
            .concatMap { x ->
                if (x == 2) Observable.error(IllegalArgumentException("error"))
                else Observable.just(x)
            }
            .map(Any::toString)
            .toList()
            .test()
            .assertError(IllegalArgumentException::class.java)
            .assertNotComplete()
    }

    @Test
    fun `observable test`() {
        Observable
            .range(1, 3)
            .concatMap { x ->
                if (x == 2) Observable.error(IllegalArgumentException("error"))
                else Observable.just(x)
            }
            .map(Any::toString)
            .test()
            .assertFailure(IllegalArgumentException::class.java, "1")
    }

    @Test
    fun `concatMapDelayError operator test`() {
        Observable
            .just(3, 0, 2, 0, 1, 0)
            .concatMapDelayError { x ->
                Observable.fromCallable { 100 / x }
            }
            .test()
            .assertError(Throwable::class.java)
            .assertValueCount(3)
            .assertNotComplete()
    }

    @Test
    fun `first test with test scheduler`() {
        val testScheduler = TestScheduler()

        val observable = Observable.just(2L, 3L, 1L)
            .flatMap {
                Observable.just(it)
                    .delay(it, TimeUnit.SECONDS, testScheduler)
            }

        val testObserver = TestObserver<Long>()
        observable.subscribe(testObserver)

        testObserver.assertNoValues()
        testScheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS)
        testObserver.assertValueCount(1)
        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        testObserver.assertValueCount(2)
        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        testObserver
            .assertValueCount(3)
            .assertValueSet(setOf(3, 1, 2))
            .assertComplete()
    }

    @Test
    fun `MyServiceWithTimeout test with test scheduler, check for timeout`() {
        val testScheduler = TestScheduler()
        val myServiceMock = mockReturning(Observable.never(), testScheduler)

        val testObserver = myServiceMock
            .externalCall()
            .test()

        testScheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS)
        testObserver.assertNotTerminated()
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)
        testObserver
            .assertComplete()
            .assertNoValues()
    }

    @Test
    fun `value is returned just before timeout`() {
        val testScheduler = TestScheduler()
        val slow = Observable
            .timer(950, TimeUnit.MILLISECONDS, testScheduler)
            .map { LocalDate.now() }
        val myService = mockReturning(slow, testScheduler)

        val testObserver = myService.externalCall()
            .test()

        testScheduler.advanceTimeBy(930, TimeUnit.MILLISECONDS)
        testObserver
            .assertNotComplete()
            .assertNoValues()
        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        testObserver
            .assertComplete()
            .assertValueCount(1)
    }

    private fun mockReturning(
        result: Observable<LocalDate>,
        testScheduler: TestScheduler
    ): MyServiceWithTimeout {
        val mock: MyService = mock(MyService::class.java)
        given(mock.externalCall()).willReturn(result)

        return MyServiceWithTimeout(mock, testScheduler)
    }
}

interface MyService {
    fun externalCall(): Observable<LocalDate>
}

class MyServiceWithTimeout(
    private val delegate: MyService,
    private val scheduler: Scheduler,
) : MyService {

    override fun externalCall(): Observable<LocalDate> {
        return delegate
            .externalCall()
            .timeout(1, TimeUnit.SECONDS, scheduler, Observable.empty())
    }
}
