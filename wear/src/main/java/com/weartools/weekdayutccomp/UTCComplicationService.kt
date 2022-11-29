/*
 * Copyright 2022 amoledwatchfaces™
 * support@amoledwatchfaces.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weartools.weekdayutccomp

import android.app.PendingIntent
import android.icu.util.TimeZone
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService


class UTCComplicationService : SuspendingComplicationDataSourceService() {

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

    val city = prefs.getString(getString(R.string.wc_setting_key), "UTC").toString()
    val zonearray = resources.getStringArray(R.array.cities).indexOf(city)
    val timezone = resources.getStringArray(R.array.zoneids)[zonearray]


    val text = TimeFormatComplicationText.Builder(format = fmt)
        .setTimeZone(TimeZone.getTimeZone(timezone))
        .build()


    return when (request.complicationType) {

        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = text,
            contentDescription = PlainComplicationText.Builder(text = getString(R.string.wc_comp_name_1))
                .build()
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = city
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
                    text = city
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
    private const val TAG = "WorldClock1"
}
}

