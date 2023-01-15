package io.obolonsky.rxstudy

sealed class UiEvent {

    data class SubmitEvent(
        val name: String,
    ) : UiEvent()

    data class CheckNameEvent(
        val name: String,
    ) : UiEvent()
}

sealed class Action {

    data class SubmitAction(
        val name: String,
    ) : Action()

    data class CheckNameAction(
        val name: String,
    ) : Action()
}