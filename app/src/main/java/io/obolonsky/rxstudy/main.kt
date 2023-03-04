@file:SuppressLint("CheckResult")
package io.obolonsky.rxstudy

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
fun main() {

    completableFutureInteroperabilityDemo()

    Thread.sleep(10_000)
}