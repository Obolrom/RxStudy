package io.obolonsky.rxstudy

data class SubmitUiModel(
    val inProgress: Boolean,
    val success: Boolean,
    val errorMessage: String,
) {

    companion object {
        fun inProgress() = SubmitUiModel(inProgress = true, success = false, errorMessage = "")

        fun success() = SubmitUiModel(inProgress = false, success = true, errorMessage = "")

        fun error(message: String) = SubmitUiModel(inProgress = false, success = false, errorMessage = message)
    }
}
