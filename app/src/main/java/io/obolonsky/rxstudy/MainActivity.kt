package io.obolonsky.rxstudy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    private fun manageState() {
        RxView.clicks(submit_button)
            .doOnNext {
                submit_button.isEnabled = false
                progress.isVisible = true
            }
            .flatMap {
                service.setName(edit_text.text?.toString().orEmpty())
                    .subscribeOn(Schedulers.io())
                    .andThen(Observable.just(Unit))
//                    .andThen(Observable.error<Throwable>(Exception("Api error")))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { progress.isVisible = false }
            .subscribe(
                {
                    showSuccessToast()
                },
                {
                    submit_button.isEnabled = true
                    showFail()
                }
            )
            .also(disposables::add)
    }

    private fun showSuccessToast() {
        showToast("Succeed")
    }

    private fun showFail() {
        showToast("Failed")
    }

    private fun showToast(message: String) {
        lastToast?.cancel()

        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .also { lastToast = it }
            .show()
    }
}