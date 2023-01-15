package io.obolonsky.rxstudy

sealed class ResultEvent

sealed class SubmitResult : ResultEvent() {

    object InFlight : SubmitResult()

    object Success : SubmitResult()

    class Failure(message: String?) : SubmitResult()
}

sealed class CheckNameResult : ResultEvent() {

    object InFlight : CheckNameResult()

    object Success : CheckNameResult()

    class Failure(message: String?) : CheckNameResult()
}
