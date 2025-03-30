package com.myprojects.routinemanager.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Вспомогательный класс для добавления/удаления событий в системном календаре.
 * Требует соответствующих разрешений в манифесте:
 *
 * <uses-permission android:name="android.permission.READ_CALENDAR"/>
 * <uses-permission android:name="android.permission.WRITE_CALENDAR"/>
 *
 * А также запроса разрешений у пользователя (runtime permissions).
 */
class CalendarHelper(private val context: Context) {

    /**
     * Добавляет мероприятие в календарь.
     * Возвращает ID созданного события или null, если не удалось создать.
     */
    fun addEventToCalendar(
        calendarId: Long,  // ID календаря, куда добавляем событие
        title: String,
        description: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Long? {
        return try {
            val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().id)
            }

            val uri: Uri? = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values
            )
            uri?.lastPathSegment?.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Удаление события из календаря по ID события.
     */
    fun deleteEvent(eventId: Long) {
        try {
            val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Пример получения ID первого доступного календаря.
     * В реальном приложении стоит предоставить пользователю выбор.
     */
    fun getDefaultCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        val uri = CalendarContract.Calendars.CONTENT_URI
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                return cursor.getLong(idCol)
            }
        }
        return null
    }
}
