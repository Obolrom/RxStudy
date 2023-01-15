package io.obolonsky.rxstudy

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class ActivityLifecycleObserver<T>(
    private val lifecycle: Lifecycle,
    private val observable: Observable<T>,
    private val onNext: Consumer<T>,
) : DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    private var disposable: Disposable? = null

    override fun onStart(owner: LifecycleOwner) {
        Log.d("MY_TAG", "onStart: ")
        disposable = observable.subscribe(onNext)
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("MY_TAG", "onStop: ")
        disposable?.dispose()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        lifecycle.removeObserver(this)
    }
}

fun <T> Observable<T>.subscribeWithLifecycle(lifecycle: Lifecycle, onNext: Consumer<T>) {
    ActivityLifecycleObserver(lifecycle, this, onNext)
}