@file:RequiresApi(Build.VERSION_CODES.N)
package io.obolonsky.rxstudy

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.CompletableFuture
import java.util.function.Function

val users = mapOf(
    1 to User("Roman"),
    2 to User("John"),
    3 to User("Derek"),
)

val agencies = listOf(
    object : TravelAgency {
        override fun searchAsync(user: User, location: GeoLocation): CompletableFuture<Flight> {
            return CompletableFuture.supplyAsync {
                Flight(user, start = "Turkey", destination = location.country)
            }
        }
    },
    object : TravelAgency {
        override fun searchAsync(user: User, location: GeoLocation): CompletableFuture<Flight> {
            return CompletableFuture.supplyAsync {
                Flight(user, start = "Ukraine", destination = location.country)
            }
        }
    },
    object : TravelAgency {
        override fun searchAsync(user: User, location: GeoLocation): CompletableFuture<Flight> {
            return CompletableFuture.supplyAsync {
                Flight(user, start = "Poland", destination = location.country)
            }
        }
    },
)

data class User(
    val name: String,
)

data class GeoLocation(
    val country: String,
)

data class Flight(
    val user: User,
    val start: String,
    val destination: String,
)

data class Ticket(
    val cost: Int,
    val flight: Flight,
)

fun findByIdAsync(id: Int): CompletableFuture<User> {
    return CompletableFuture.supplyAsync {
        Thread.sleep(100)
        users[id]!!.also { println("findByIdAsync $it") }
    }
}

fun locateAsync(): CompletableFuture<GeoLocation> {
    return CompletableFuture.supplyAsync {
        Thread.sleep(120)
        GeoLocation("Canada").also { println("locateAsync $it") }
    }
}

fun bookAsync(flight: Flight): CompletableFuture<Ticket> {
    return CompletableFuture.supplyAsync {
        Ticket(110, flight).also { println("bookAsync $it") }
    }
}

interface TravelAgency {

    fun searchAsync(user: User, location: GeoLocation): CompletableFuture<Flight>
}


fun futuresDemo() {

    val userFuture: CompletableFuture<User> = findByIdAsync(2)
    val locationFuture: CompletableFuture<GeoLocation> = locateAsync()

    userFuture
        .thenCombine(locationFuture) { user, location ->
            agencies
                .stream()
                .map { agency -> agency.searchAsync(user, location) }
                .reduce { f1: CompletableFuture<Flight>, f2: CompletableFuture<Flight> ->
                    f1.applyToEither(f2, Function.identity())
                }
                .get()
        }
        .thenCompose(Function.identity())
        .thenCompose(::bookAsync)
        .thenApply { ticket ->
            println("ticket $ticket")
            ticket
        }
}