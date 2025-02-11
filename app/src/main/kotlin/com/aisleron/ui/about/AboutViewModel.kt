package com.aisleron.ui.about

import androidx.lifecycle.ViewModel
import com.aisleron.BuildConfig

class AboutViewModel : ViewModel() {

    val versionName: String = BuildConfig.VERSION_NAME

}