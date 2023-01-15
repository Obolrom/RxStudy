package io.obolonsky.rxstudy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var lastToast: Toast? = null

    private val service = Service()

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manageState()
    }

    private fun submitEventTransformer(): ObservableTransformer<Action.SubmitAction, SubmitResult> {
        return ObservableTransformer { eventObservable ->
            eventObservable
                .flatMap { action ->
                    service.setName(action.name)
                        .subscribeOn(Schedulers.io())
                        .andThen(Observable.just(Unit))
//                        .andThen(Observable.error<Throwable>(Exception("Api error")))
                        .map<SubmitResult> { SubmitResult.Success }
                        .onErrorReturn { SubmitResult.Failure(it.message.orEmpty()) }
                        .observeOn(AndroidSchedulers.mainThread()) // why Jake put that here ?
                        .startWith(SubmitResult.InFlight)
                }
        }
    }

    private fun checkNameTransformer(): ObservableTransformer<Action.CheckNameAction, CheckNameResult> {
        return ObservableTransformer { eventObservable ->
            eventObservable
                .switchMap { event ->
                    Observable.just(event)
                        .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .flatMap {
                            service.checkName()
                                .andThen(Observable.just(Unit))
                        }
                        .map<CheckNameResult> { CheckNameResult.Success }
                        .onErrorReturn { CheckNameResult.Failure(it.message) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .startWith(CheckNameResult.InFlight)
                }
        }
    }

    private fun combinedTransformer(): ObservableTransformer<UiEvent, ResultEvent> {
        return ObservableTransformer { events ->
            events.publish { shared ->
                Observable.merge(
                    shared
                        .ofType(UiEvent.SubmitEvent::class.java)
                        .map { Action.SubmitAction(it.name) }
                        .compose(submitEventTransformer()),
                    shared
                        .ofType(UiEvent.CheckNameEvent::class.java)
                        .map { Action.CheckNameAction(it.name) }
                        .compose(checkNameTransformer()),
                )
            }
        }
    }

    private fun manageState() {
        val clicksObservable = RxView.clicks(submit_button)
            .map { UiEvent.SubmitEvent(edit_text.text.toString()) }

        val checkNameObservable = RxTextView.textChanges(edit_text)
            .map { text -> UiEvent.CheckNameEvent(text.toString()) }

        val events = Observable.merge(clicksObservable, checkNameObservable)

        val uiModels = events
            .compose(combinedTransformer())
            .scan(SubmitUiModel.idle()) { _, result ->
                when (result) {
                    is SubmitResult.InFlight, is CheckNameResult.InFlight -> {
                        SubmitUiModel.inProgress()
                    }

                    is CheckNameResult.Success -> {
                        SubmitUiModel.idle()
                    }

                    is SubmitResult.Success -> {
                        SubmitUiModel.success()
                    }

                    else -> throw IllegalArgumentException("Unknown result: $result")
                }
            }

        uiModels
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