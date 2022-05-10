package com.automate123.videshorts.data

import android.content.Context
import androidx.annotation.Keep
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.autsoft.krate.SimpleKrate
import hu.autsoft.krate.default.withDefault
import hu.autsoft.krate.stringPref
import javax.inject.Inject

@Keep
class Preferences @Inject constructor(@ApplicationContext context: Context) : SimpleKrate(context) {

    var duration by stringPref("duration").withDefault("2")
}