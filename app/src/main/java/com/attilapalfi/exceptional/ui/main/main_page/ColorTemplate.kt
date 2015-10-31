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
    public val ChartColors = ArrayList<Int>(27)
    @ColorInt
    public val Black: Int
    @ColorInt
    public val Blue: Int
    @ColorInt
    public val ExceptionalRed: Int

    init {
        Injector.INSTANCE.applicationComponent.inject(this)

        ChartColors.add(ContextCompat.getColor(context, R.color.indigo_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.blue_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.teal_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.green_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.lime_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.yellow_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.orange_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.grey_400))
        ChartColors.add(ContextCompat.getColor(context, R.color.purple_400))

        ChartColors.add(ContextCompat.getColor(context, R.color.indigo_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.blue_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.teal_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.green_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.lime_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.yellow_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.orange_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.grey_200))
        ChartColors.add(ContextCompat.getColor(context, R.color.purple_200))

        ChartColors.add(ContextCompat.getColor(context, R.color.indigo_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.blue_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.teal_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.green_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.lime_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.yellow_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.orange_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.grey_600))
        ChartColors.add(ContextCompat.getColor(context, R.color.purple_600))

        Black = ContextCompat.getColor(context, R.color.black)
        Blue = ContextCompat.getColor(context, R.color.blue_400)
        ExceptionalRed = ContextCompat.getColor(context, R.color.exceptional_red)
    }
}