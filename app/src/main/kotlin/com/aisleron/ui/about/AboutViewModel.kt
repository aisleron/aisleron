package com.aisleron.ui.about

import androidx.lifecycle.ViewModel
import com.aisleron.BuildConfig

class AboutViewModel : ViewModel() {

    val versionName: String get() = BuildConfig.VERSION_NAME

}