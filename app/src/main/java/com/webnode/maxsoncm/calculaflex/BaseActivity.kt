package com.webnode.maxsoncm.calculaflex

import android.support.v7.app.AppCompatActivity
import com.webnode.maxsoncm.calculaflex.utils.CalculaFlexTracker
import com.webnode.maxsoncm.calculaflex.utils.ScreenMap

open class BaseActivity : AppCompatActivity() {

    open fun getScreenName(): String {
        return ScreenMap.getScreenNameBy(this)
    }


    override fun onStart() {
        super.onStart()
        CalculaFlexTracker.trackScreen(this, getScreenName())
    }
}
