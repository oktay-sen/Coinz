package com.oktaysen.coinz.backend.util

import com.google.firebase.Timestamp
import java.util.*

fun today(): Timestamp = Timestamp.now()

fun yesterday(): Timestamp = {
    val cal = Calendar.getInstance()
    cal.time = today().toDate()
    cal.add(Calendar.DAY_OF_MONTH, -1)
    Timestamp(cal.time)
}()

fun tomorrow(): Timestamp = {
    val cal = Calendar.getInstance()
    cal.time = today().toDate()
    cal.add(Calendar.DAY_OF_MONTH, 1)
    Timestamp(cal.time)
}()