package io.obolonsky.rxstudy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var lastToast: Toast? = null

    private val service = Service()

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manageState()
    }

    // uncomment line with 38, and comment 37th
    // to see when happens if api call failed
    private fun manageState() {
        RxView.clicks(submit_button)
            .map { SubmitEvent(edit_text.text.toString()) }
            .flatMap { event ->
                service.setName(event.name)
                    .subscribeOn(Schedulers.io())
                    .andThen(Observable.just(Unit))
//                    .andThen(Observable.error<Throwable>(Exception("Api error")))
                    .map { SubmitUiModel.success() }
                    .onErrorReturn { SubmitUiModel.error(it.message.orEmpty()) }
                    .observeOn(AndroidSchedulers.mainThread()) // why Jake put that here ?
                    .startWith(SubmitUiModel.inProgress())
            }
            .subscribe(
                { model ->
                    submit_button.isEnabled = !model.inProgress
                    progress.isVisible = model.inProgress
                    if (!model.inProgress) {
                        if (model.success) {
                            showSuccessToast()
                        } else {
                            showFail(model.errorMessage)
                        }
                    }
                },
                { throw OnErrorNotImplementedException(it) }
            )
            .also(disposables::add)
    }

    private fun showSuccessToast() {
        showToast("Succeed")
    }

    private fun showFail(message: String? = null) {
        showToast(message ?: "Failed")
    }

    private fun showToast(message: String) {
        lastToast?.cancel()

        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .also { lastToast = it }
            .show()
    }
}