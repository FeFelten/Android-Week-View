package com.alamkanak.weekview

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

data class WeekViewEvent<T> internal constructor(
    internal val id: Long = 0L,
    internal val titleResource: TextResource,
    internal val locationResource: TextResource? = null,
    internal val startTime: LocalDateTime = LocalDateTime.now(),
    internal val endTime: LocalDateTime = LocalDateTime.now(),
    internal val isAllDay: Boolean = false,
    internal val style: Style = Style(),
    internal val data: T
) : WeekViewDisplayable<T> {

    override fun toWeekViewEvent(): WeekViewEvent<T> = this

    internal sealed class ColorResource {
        data class Value(@ColorInt val color: Int) : ColorResource()
        data class Id(@ColorRes val resId: Int) : ColorResource()

        fun resolve(context: Context): Int = when (this) {
            is Id -> ContextCompat.getColor(context, resId)
            is Value -> color
        }
    }

    internal sealed class TextResource {
        data class Value(val text: CharSequence) : TextResource()
        data class Id(@StringRes val resId: Int) : TextResource()

        fun resolve(context: Context, shouldSetBold: Boolean): CharSequence = when (this) {
            is Id -> {
                val text = context.getString(resId)
                if (shouldSetBold) text.bold() else text
            }
            is Value -> when (text) {
                // We don't change the existing style of SpannableStrings.
                is SpannableString -> text
                is SpannableStringBuilder -> text.build()
                else -> if (shouldSetBold) text.bold() else text
            }
        }
    }

    internal sealed class DimenResource {
        data class Value(val value: Int) : DimenResource()
        data class Id(@DimenRes val resId: Int) : DimenResource()

        fun resolve(context: Context): Int = when (this) {
            is Id -> context.resources.getDimensionPixelSize(resId)
            is Value -> value
        }
    }

    class Style {

        internal var backgroundColorResource: ColorResource? = null
        internal var textColorResource: ColorResource? = null
        internal var borderWidthResource: DimenResource? = null
        internal var borderColorResource: ColorResource? = null

        @Deprecated("Remove with setTextStrikeThrough() in the future.")
        internal var isTextStrikeThrough: Boolean = false

        class Builder {

            private val style = Style()

            @PublicApi
            fun setBackgroundColor(@ColorInt color: Int): Builder {
                style.backgroundColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setBackgroundColorResource(@ColorRes resId: Int): Builder {
                style.backgroundColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            fun setTextColor(@ColorInt color: Int): Builder {
                style.textColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setTextColorResource(@ColorRes resId: Int): Builder {
                style.textColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            @Deprecated("Use a SpannableString for the title or location instead. This method will be removed in the future.")
            fun setTextStrikeThrough(strikeThrough: Boolean): Builder {
                style.isTextStrikeThrough = strikeThrough
                return this
            }

            @PublicApi
            fun setBorderWidth(width: Int): Builder {
                style.borderWidthResource = DimenResource.Value(width)
                return this
            }

            @PublicApi
            fun setBorderWidthResource(@DimenRes resId: Int): Builder {
                style.borderWidthResource = DimenResource.Id(resId)
                return this
            }

            @PublicApi
            fun setBorderColor(@ColorInt color: Int): Builder {
                style.borderColorResource = ColorResource.Value(color)
                return this
            }

            @PublicApi
            fun setBorderColorResource(@ColorRes resId: Int): Builder {
                style.borderColorResource = ColorResource.Id(resId)
                return this
            }

            @PublicApi
            fun build(): Style = style
        }
    }

    class Builder<T : Any>(
        private val data: T
    ) {

        private var id: Long? = null
        private var title: TextResource? = null
        private var location: TextResource? = null
        private var startTime: LocalDateTime? = null
        private var endTime: LocalDateTime? = null
        private var style: Style? = null
        private var isAllDay: Boolean = false

        @PublicApi
        fun setId(id: Long): Builder<T> {
            this.id = id
            return this
        }

        @PublicApi
        fun setTitle(title: CharSequence): Builder<T> {
            this.title = TextResource.Value(title)
            return this
        }

        @PublicApi
        fun setTitle(resId: Int): Builder<T> {
            this.title = TextResource.Id(resId)
            return this
        }

        @PublicApi
        fun setLocation(location: CharSequence): Builder<T> {
            this.location = TextResource.Value(location)
            return this
        }

        @PublicApi
        fun setLocation(resId: Int): Builder<T> {
            this.location = TextResource.Id(resId)
            return this
        }

        @PublicApi
        fun setStartTime(startTime: LocalDateTime): Builder<T> {
            this.startTime = startTime
            return this
        }

        @PublicApi
        @Deprecated(
            message = "Use setStartTime(LocalDateTime) instead. This method will be removed in the future.",
            replaceWith = ReplaceWith("setStartTime(LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault()))", "java.time.LocalDateTime", "java.time.ZoneId")
        )
        fun setStartTime(startTime: Calendar): Builder<T> {
            return setStartTime(LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault()))
        }

        @PublicApi
        fun setEndTime(endTime: LocalDateTime): Builder<T> {
            this.endTime = endTime
            return this
        }

        @PublicApi
        @Deprecated(
            message = "Use setEndTime(LocalDateTime) instead. This method will be removed in the future.",
            replaceWith = ReplaceWith("setEndTime(LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault()))", "java.time.LocalDateTime", "java.time.ZoneId")
        )
        fun setEndTime(endTime: Calendar): Builder<T> {
            return setEndTime(LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault()))
        }

        @PublicApi
        fun setStyle(style: Style): Builder<T> {
            this.style = style
            return this
        }

        @PublicApi
        fun setAllDay(isAllDay: Boolean): Builder<T> {
            this.isAllDay = isAllDay
            return this
        }

        @PublicApi
        fun build(): WeekViewEvent<T> {
            val id = checkNotNull(id) { "id cannot be null" }
            val title = checkNotNull(title) { "title cannot be null" }
            val startTime = checkNotNull(startTime) { "startTime cannot be null" }
            val endTime = checkNotNull(endTime) { "endTime cannot be null" }
            val style = this.style ?: Style.Builder().build()
            return WeekViewEvent(id, title, location, startTime, endTime, isAllDay, style, data)
        }
    }
}
