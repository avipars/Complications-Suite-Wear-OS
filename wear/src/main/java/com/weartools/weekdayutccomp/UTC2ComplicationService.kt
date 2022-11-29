package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.icu.util.TimeZone
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService


class UTC2ComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }


    private fun openScreen(): PendingIntent? {

        val intent = packageManager.getLaunchIntentForPackage("com.weartools.weekdayutccomp")

        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

override fun getPreviewData(type: ComplicationType): ComplicationData {
    return ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(text = "10:00").build(),
        contentDescription = PlainComplicationText.Builder(text = getString(R.string.wc_comp_name_1))
            .build()
    )
        .setTitle(
            PlainComplicationText.Builder(
                text = "UTC"
            ).build()
        )
        .setTapAction(null)
        .build()
}

override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

    // TODO: TU IDU VARIABILNE
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val ismilitary = prefs.getBoolean(getString(R.string.wc_ampm_setting_key), false)
    val leadingzero = prefs.getBoolean(getString(R.string.wc_setting_leading_zero_key), true)

    val fmt = if (ismilitary && leadingzero) "HH:mm"
    else if (!ismilitary && leadingzero) "hh:mm a"
    else if (ismilitary && !leadingzero) "H:mm"
    else "h:mm a"

    val city2 = prefs.getString(getString(R.string.wc2_setting_key), "UTC").toString()
    val zonearray2 = resources.getStringArray(R.array.cities).indexOf(city2)
    val timezone2 = resources.getStringArray(R.array.zoneids)[zonearray2]


    val text = TimeFormatComplicationText.Builder(format = fmt)
        .setTimeZone(TimeZone.getTimeZone(timezone2))
        .build()


    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.wc_comp_name_1))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = city2
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText
                .Builder(text = getString(R.string.wc_comp_name_1))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = city2
                ).build()
            )
            .setTapAction(openScreen())
            .build()

        else -> {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unexpected complication type ${request.complicationType}")
            }
            null
        }

    }
}

override fun onComplicationDeactivated(complicationInstanceId: Int) {
    Log.d(TAG, "onComplicationDeactivated(): $complicationInstanceId")
}

companion object {
    private const val TAG = "WorldClock2"
}
}
