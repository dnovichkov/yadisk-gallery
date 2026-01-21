package com.dnovichkov.yadiskgallery

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for YaDisk Gallery.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class YaDiskGalleryApp : Application()
