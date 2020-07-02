package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.OnEmptyViewLongClickListener
import com.alamkanak.weekview.OnEventClickListener
import com.alamkanak.weekview.OnEventLongClickListener
import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.lazyView
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class LimitedActivity : AppCompatActivity(), OnEventClickListener<Event>,
    OnMonthChangeListener<Event>, OnEventLongClickListener<Event>, OnEmptyViewLongClickListener {

    private val weekView: WeekView<Event> by lazyView(R.id.weekView)
    private val database: EventsDatabase by lazy { EventsDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limited)

        toolbar.setupWithWeekView(weekView)

        weekView.onEventClickListener = this
        weekView.onMonthChangeListener = this
        weekView.onEventLongClickListener = this
        weekView.onEmptyViewLongClickListener = this

        setupDateRange()
    }

    private fun setupDateRange() {
        weekView.minDate = YearMonth.now().atDay(1)
        weekView.maxDate = YearMonth.now().atEndOfMonth()
    }

    override fun onMonthChange(
        startDate: LocalDate,
        endDate: LocalDate
    ) = database.getEventsInRange(startDate, endDate)

    override fun onEventClick(data: Event, eventRect: RectF) {
        showToast("Clicked ${data.title}")
    }

    override fun onEventLongClick(data: Event, eventRect: RectF) {
        showToast("Long-clicked ${data.title}")
        Toast.makeText(this, "Long pressed event: " + data.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        // val sdf = SimpleDateFormat.getDateTimeInstance()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
        showToast("Empty view long-clicked at ${formatter.format(time)}")
    }
}
