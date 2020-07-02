package com.alamkanak.weekview.sample.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

inline fun <reified T : View> Activity.lazyView(
    @IdRes viewId: Int
): Lazy<T> = lazy { findViewById<T>(viewId) }

inline fun <reified T : View> Fragment.lazyView(
    @IdRes viewId: Int
): Lazy<T> = lazy { requireActivity().findViewById<T>(viewId) }

fun LocalDate.toCalendar(): Calendar {
    val instant = atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = instant.toEpochMilli()
    return calendar
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observe: (T) -> Unit) {
    observe(owner, Observer { observe(it) })
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
