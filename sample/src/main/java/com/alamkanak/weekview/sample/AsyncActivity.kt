package com.alamkanak.weekview.sample

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.sample.data.EventsApi
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.alamkanak.weekview.sample.util.lazyView
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.android.synthetic.main.view_toolbar.toolbar

private data class AsyncViewState(
    val events: List<ApiEvent> = emptyList(),
    val isLoading: Boolean = false
)

private class AsyncViewModel(
    private val eventsApi: EventsApi
) {
    val viewState = MutableLiveData<AsyncViewState>()

    init {
        viewState.value = AsyncViewState(isLoading = true)
        fetchEvents()
    }

    fun fetchEvents() = eventsApi.fetchEvents {
        viewState.value = AsyncViewState(it)
    }

    fun remove(event: ApiEvent) {
        val allEvents = viewState.value?.events ?: return
        val event2 = WeekViewEvent.Builder(1).build()
        viewState.value = AsyncViewState(events = allEvents.minus(event))
    }
}

class AsyncActivity : AppCompatActivity() {

    private val weekView: WeekView<ApiEvent> by lazyView(R.id.weekView)

    private val viewModel: AsyncViewModel by lazy {
        AsyncViewModel(EventsApi(this))
    }

    @Suppress("DEPRECATION")
    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Loading events ...")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        viewModel.viewState.observe(this, Observer { viewState ->
            if (viewState.isLoading) {
                progressDialog.show()
            } else {
                progressDialog.dismiss()
            }

            weekView.submit(viewState.events)
        })

        weekView.setOnEventClickListener { event, _ ->
            viewModel.remove(event)
            showToast("Removed ${event.title}")
        }

        weekView.setOnEventLongClickListener { event, _ ->
            showToast("Long-clicked ${event.title}")
        }

        weekView.setOnEmptyViewLongClickListener { time ->
            // val sdf = SimpleDateFormat.getDateTimeInstance()
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            showToast("Empty view long-clicked at ${formatter.format(time)}")
        }
    }
}
