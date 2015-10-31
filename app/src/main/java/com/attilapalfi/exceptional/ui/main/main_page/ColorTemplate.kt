package com.attilapalfi.exceptional.ui.main.main_page

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-27.
 */
object ColorTemplate {
    @Inject
    lateinit var context: Context
    public val DarkChartColors = ArrayList<Int>(27)
    public val LightChartColors = ArrayList<Int>(27)
    @ColorInt
    public var Black: Int
    @ColorInt
    public var Blue: Int
    @ColorInt
    public var ExceptionalRed: Int

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        initDarkChartColors()
        initLightChartColors()

        Black = ContextCompat.getColor(context, R.color.black)
        Blue = ContextCompat.getColor(context, R.color.blue_400)
        ExceptionalRed = ContextCompat.getColor(context, R.color.exceptional_red)
    }

    private fun initDarkChartColors() {
        DarkChartColors.add(ContextCompat.getColor(context, R.color.blue_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.teal_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.green_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.lime_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.indigo_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.yellow_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.orange_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.grey_600))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.purple_600))

        DarkChartColors.add(ContextCompat.getColor(context, R.color.blue_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.teal_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.green_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.lime_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.indigo_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.yellow_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.orange_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.grey_400))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.purple_400))

        DarkChartColors.add(ContextCompat.getColor(context, R.color.blue_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.teal_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.green_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.lime_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.indigo_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.yellow_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.orange_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.grey_200))
        DarkChartColors.add(ContextCompat.getColor(context, R.color.purple_200))
    }

    private fun initLightChartColors() {
        LightChartColors.add(ContextCompat.getColor(context, R.color.blue_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.teal_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.green_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.lime_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.indigo_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.yellow_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.orange_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.grey_200))
        LightChartColors.add(ContextCompat.getColor(context, R.color.purple_200))

        LightChartColors.add(ContextCompat.getColor(context, R.color.blue_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.teal_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.green_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.lime_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.indigo_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.yellow_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.orange_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.grey_400))
        LightChartColors.add(ContextCompat.getColor(context, R.color.purple_400))

        LightChartColors.add(ContextCompat.getColor(context, R.color.blue_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.teal_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.green_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.lime_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.indigo_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.yellow_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.orange_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.grey_600))
        LightChartColors.add(ContextCompat.getColor(context, R.color.purple_600))
    }
}