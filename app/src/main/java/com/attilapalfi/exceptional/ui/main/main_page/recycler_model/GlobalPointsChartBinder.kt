package com.attilapalfi.exceptional.ui.main.main_page.recycler_model

import android.view.View
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.StatSupplier
import com.attilapalfi.exceptional.ui.main.main_page.ColorTemplate
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-31.
 */
class GlobalPointsChartBinder : RowItemBinder() {
    override val rowType = RowType.GLOBAL_POINTS_CHART

    @Inject
    lateinit var statSupplier: StatSupplier
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var metadataStore: MetadataStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun bindRow(rowView: View) {
        val globalPoints = statSupplier.globalPoints
        if (globalPoints.isNotEmpty()) {
            val pieData = getPieData(globalPoints)
            initChart(pieData, rowView)
            //            async {
            //                val pieData = getPieData(globalPoints)
            //                uiThread {
            //                    initChart(pieData, rowView)
            //                }
            //            }
        }
    }

    private fun getPieData(globalPoints: LinkedHashMap<BigInteger, Int>): BarData {
        val (valueList, userIndex) = getValueListAndIndex(globalPoints)
        val barDataSet = createBarDataSet(valueList)
        setColors(barDataSet, userIndex, valueList)
        return createBarData(barDataSet, userIndex, valueList)
    }

    private fun getValueListAndIndex(globalPoints: LinkedHashMap<BigInteger, Int>): Pair<ArrayList<Int>, Int> {
        val user = metadataStore.user
        val valueList = ArrayList(globalPoints.values.toList())
        val userIndex = getUserIndex(valueList, user)
        valueList.add(userIndex, user.points)
        return Pair(valueList, userIndex)
    }

    private fun createBarDataSet(valueList: ArrayList<Int>): BarDataSet {
        val barDataSet = BarDataSet(
                valueList.mapIndexed { index, value ->
                    BarEntry(value.toFloat(), index)
                },
                "")
        return barDataSet
    }

    private fun setColors(barDataSet: BarDataSet, userIndex: Int, valueList: ArrayList<Int>) {
        val colorList = ArrayList<Int>(valueList.size)
        valueList.forEachIndexed { index, value ->
            if ( index == userIndex ) {
                colorList.add(ColorTemplate.ExceptionalRed)
            } else {
                colorList.add(ColorTemplate.Blue)
            }
        }
        barDataSet.colors = colorList
    }

    private fun createBarData(barDataSet: BarDataSet, userIndex: Int, valueList: ArrayList<Int>): BarData {
        return BarData(valueList.mapIndexed {
            index, value ->
            if ( index == userIndex ) {
                "You"
            } else {
                ""
            }
        }, barDataSet)
    }

    private fun getUserIndex(friendPoints: List<Int>, user: Friend): Int {
        var userIndex = Collections.binarySearch(friendPoints, user.points)
        if ( userIndex < 0 ) {
            return -userIndex - 1
        } else {
            return userIndex
        }
    }

    private fun initChart(barData: BarData, rowView: View) {
        val chart = rowView.findViewById(R.id.global_points_bar_chart) as BarChart
        chart.setDescription("")
        chart.legend.isEnabled = false
        chart.data = barData
        chart.animateXY(900, 900)
    }
}