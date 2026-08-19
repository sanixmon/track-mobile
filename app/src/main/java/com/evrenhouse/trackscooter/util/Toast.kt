package com.evrenhouse.trackscooter.util

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}
