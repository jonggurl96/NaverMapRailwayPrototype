package com.example.prototype

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** 앱 전역 Hilt 컴포넌트를 생성하는 Application 진입점이다. */
@HiltAndroidApp
class PrototypeApplication : Application()
