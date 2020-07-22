package com.alamkanak.weekview.sample.data

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

class EventsApi(
    private val context: Context
) {

    private val responseType = object : TypeToken<List<ApiEvent>>() {}.type

    fun fetchEvents(
        onSuccess: (List<WeekViewDisplayable<Any>>) -> Unit
    ) {
        AsyncTask.execute {
/*            sleep(2_000)
            val inputStream = context.assets.open("events.json")
            val json = inputStream.reader().readText()

            val activity = context as Activity
            activity.runOnUiThread {
                onSuccess(Gson().fromJson(json, responseType))
            }*/

            val list1: MutableList<WeekViewDisplayable<Any>> = ArrayList()
            val now = Calendar.getInstance()

            for (i in 0..19) {
                val startTime = now.clone() as Calendar
                startTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
                startTime.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR))
                startTime.set(Calendar.HOUR_OF_DAY, i + 1)

                val endTime = startTime.clone() as Calendar
                endTime.set(Calendar.YEAR, startTime.get(Calendar.YEAR))
                endTime.set(Calendar.DAY_OF_YEAR, startTime.get(Calendar.DAY_OF_YEAR))
                endTime.set(Calendar.HOUR_OF_DAY, i + 2)
                val builder: WeekViewEvent.Builder<Any> = WeekViewEvent.Builder(Any())
                        .setId(i + 1L)
                        .setTitle("DUMMY title")
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setAllDay(false)
                list1.add(builder.build())
            }
            val list2: MutableList<WeekViewDisplayable<Any>> = ArrayList()
            for (i in 0..19) {
                val startTime = now.clone() as Calendar
                startTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
                startTime.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) + 1)
                startTime.set(Calendar.HOUR_OF_DAY, i + 1)

                val endTime = startTime.clone() as Calendar
                endTime.set(Calendar.YEAR, startTime.get(Calendar.YEAR))
                endTime.set(Calendar.DAY_OF_YEAR, startTime.get(Calendar.DAY_OF_YEAR) + 1)
                endTime.set(Calendar.HOUR_OF_DAY, i + 2)
                val builder: WeekViewEvent.Builder<Any> = WeekViewEvent.Builder(Any())
                        .setId(i + 1L)
                        .setTitle("DUMMY title 2")
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setAllDay(false)
                list2.add(builder.build())
            }
            val manual = AtomicBoolean(false)
            val handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable {
                override fun run() {
                    if (manual.get()) {
                        manual.set(false)
                        onSuccess(list1)
                    } else {
                        manual.set(true)
                        onSuccess(list2)
                    }
                    handler.postDelayed(this, 50L)
                }
            })
        }
    }
}
