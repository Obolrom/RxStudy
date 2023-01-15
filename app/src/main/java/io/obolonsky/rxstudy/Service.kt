package io.obolonsky.rxstudy

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

// emulates some kind of repository
class Service : UserManager {

    private val fakeRemoteSource = BehaviorSubject.createDefault(User("Jake", 33))

    override fun getUser(): Observable<User> {
        return fakeRemoteSource
    }

    override fun setAge(age: Int): Completable {
        return Completable.fromAction {
            fakeRemoteSource.value?.let { user ->
                fakeRemoteSource.onNext(user.copy(age = age))
            }
        }
            .delay(2, TimeUnit.SECONDS)
    }

    override fun setName(name: String): Completable {
        return Single.fromCallable {
            fakeRemoteSource.value?.let { user ->
                fakeRemoteSource.onNext(user.copy(name = name))
            }
        }
            .ignoreElement()
            .delay(2, TimeUnit.SECONDS)
    }

    fun checkName(): Completable {
        return Completable.complete()
    }
}