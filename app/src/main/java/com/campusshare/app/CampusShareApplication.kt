package com.campusshare.app

import android.app.Application
import com.campusshare.app.data.CampusShareData

class CampusShareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CampusShareData.initialize(this)
    }
}
