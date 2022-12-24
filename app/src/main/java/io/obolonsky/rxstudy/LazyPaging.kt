@file:SuppressLint("CheckResult")
package io.obolonsky.rxstudy

import android.annotation.SuppressLint
import io.reactivex.Observable

const val PAGE_SIZE = 3

val personTable = listOf(
    "Person 1",
    "Person 2",
    "Person 3",
    "Person 4",
    "Person 5",
    "Person 6",
    "Person 7",
    "Person 8",
    "Person 9",
    "Person 10",
    "Person 11",
    "Person 12",
    "Person 13",
    "Person 14",
)

fun getChunkOfPersons(page: Int): List<String> {
    println("getChunk page: $page")

    if (personTable.size < PAGE_SIZE * page) return emptyList()

    val endIndex = if (personTable.size < PAGE_SIZE * (page + 1))
        personTable.size
    else
        PAGE_SIZE * (page + 1)

    return personTable.subList(PAGE_SIZE * page, endIndex)
}

fun lazyPaging(page: Int): Observable<String> {
    return Observable.defer { Observable.fromIterable(getChunkOfPersons(page)) }
        .concatWith(Observable.defer { lazyPaging(page + 1) })
}

fun lazyPagingList(): Observable<List<String>> {
    return Observable.range(2, 100)
        .map(::getChunkOfPersons)
        .takeWhile { it.isNotEmpty() }
}

fun testLazyPaging1() {
    lazyPaging(1)
        .take(5) // check invokes of getChunkOfPersons with different count size
        .subscribe {
            println(it)
        }
}

fun testLazyPaging2() {
    lazyPagingList()
        .subscribe {
            println(it)
        }
}

fun testLazyPaging3() {
    lazyPagingList()
        .toList()
        .subscribe { it, _ ->
            println(it)
        }
}

// function to test api
fun callMe() {
//    testLazyPaging1()

//    testLazyPaging2()

    testLazyPaging3()
}