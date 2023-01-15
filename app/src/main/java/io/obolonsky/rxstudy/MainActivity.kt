package io.obolonsky.rxstudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val subj = Observable.interval(1, TimeUnit.SECONDS)

    private val state = BehaviorSubject.createDefault("someState")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        state
            .subscribeWithLifecycle(lifecycle) {
                Log.d("MY_TAG", "onNext: $it")
            }
    }
}