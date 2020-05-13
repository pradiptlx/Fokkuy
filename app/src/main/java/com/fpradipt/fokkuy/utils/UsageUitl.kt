package com.fpradipt.fokkuy.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.core.text.HtmlCompat
import com.fpradipt.fokkuy.R
import com.fpradipt.fokkuy.model.UsageModel
import java.text.SimpleDateFormat



/**
 * Take the Long milliseconds returned by the system and stored in Room,
 * and convert it to a nicely formatted string for display.
 *
 * EEEE - Display the long letter version of the weekday
 * MMM - Display the letter abbreviation of the nmotny
 * dd-yyyy - day in month and full year numerically
 * HH:mm - Hours and minutes in 24hr format
 */
@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("EEEE MMM-dd-yyyy' Time: 'HH:mm")
        .format(systemTime).toString()
}

fun formatLog(log: List<UsageModel>, resources: Resources): Spanned {
    Log.d("FORMAT LOG", log.toString())
    val sb = StringBuilder()
    sb.apply {
        append(resources.getString(R.string.title))
        log.forEach {
            append("<br>")
            append(resources.getString(R.string.start_time))
            append("\t${convertLongToDateString(it.startTimer)}<br>")
            if (it.endTimer != it.startTimer) {
                append(resources.getString(R.string.end_time))
                append("\t${convertLongToDateString(it.endTimer)}<br>")
                append("<b>Duration: </b> ${it.duration} seconds<br>")
                append("<b>Created At: </b>${it.createdAt}<br><br>")
            }
        }
    }
    return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
}
