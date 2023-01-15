package io.obolonsky.rxstudy

import io.reactivex.Completable
import io.reactivex.Observable


interface UserManager {

    fun getUser(): Observable<User>

    fun setAge(age: Int): Completable

    fun setName(name: String): Completable
}