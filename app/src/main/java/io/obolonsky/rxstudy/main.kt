@file:SuppressLint("CheckResult")
package io.obolonsky.rxstudy

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import io.obolonsky.rxstudy.chapter6.test
import io.obolonsky.rxstudy.chapter6.testBufferingEvents

@RequiresApi(Build.VERSION_CODES.N)
fun main() {
    testBufferingEvents()

    Thread.sleep(10_000)
}