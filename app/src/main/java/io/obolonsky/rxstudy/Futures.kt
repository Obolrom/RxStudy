package io.obolonsky.rxstudy

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

val users = mapOf(
    1 to User("Roman"),
    2 to User("John"),
    3 to User("Derek"),
)

val agencies = listOf(
    object : TravelAgency {
        override fun search(user: User, location: GeoLocation): Flight {
            return Flight(user, start = "Turkey", destination = location.country)
        }
    },
    object : TravelAgency {
        override fun search(user: User, location: GeoLocation): Flight {
            return Flight(user, start = "Ukraine", destination = location.country)
        }
    },
    object : TravelAgency {
        override fun search(user: User, location: GeoLocation): Flight {
            return Flight(user, start = "Poland", destination = location.country)
        }
    }
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

fun findById(id: Int): User {
    return users[id]!!.also { println("findById $it") }
}

fun locate(): GeoLocation {
    return GeoLocation("Canada").also { println("locate $it") }
}

fun book(flight: Flight): Ticket {
    return Ticket(110, flight)
}

interface TravelAgency {

    fun search(user: User, location: GeoLocation): Flight
}


@RequiresApi(Build.VERSION_CODES.N)
fun futuresDemo() {

    val pool = Executors.newFixedThreadPool(10)

    val user: User = findById(2)
    val location: GeoLocation = locate()
    val ecs: ExecutorCompletionService<Flight> = ExecutorCompletionService(pool)

    agencies
        .forEach { agency ->
            ecs.submit {
                agency.search(user, location)
                    .also { println("search $it") }
            }
        }

    val firstFlight: Future<Flight> = ecs.poll(5, TimeUnit.SECONDS)
    val flight = firstFlight.get()
    println("flight $flight")
    val ticket = book(flight)
    println("ticket $ticket")
}