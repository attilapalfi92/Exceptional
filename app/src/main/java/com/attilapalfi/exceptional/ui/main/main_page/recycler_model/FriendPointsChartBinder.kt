package com.attilapalfi.exceptional.ui.main.main_page.recycler_model

import android.view.View
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.ui.main.main_page.ColorTemplate
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-27.
 */
public class FriendPointsChartBinder : RowItemBinder() {
    override val rowType = RowType.FRIEND_POINTS_CHART
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var metadataStore: MetadataStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }


    override fun bindRow(rowView: View) {
        val friendList = friendStore.getStoredFriends()
        val friendNames = ArrayList(friendList.map { it.getName() })
        val friendPoints = ArrayList(friendList.map { it.points })
        val user = metadataStore.user
        val userIndex = getUserIndex(friendPoints, user)
        initChart(friendNames, friendPoints, rowView, user, userIndex)
    }

    private fun getUserIndex(friendPoints: ArrayList<Int>, user: Friend): Int {
        var userIndex = Collections.binarySearch(friendPoints, user.points, compareByDescending { it })
        if ( userIndex < 0 ) {
            return -userIndex - 1
        } else {
            return userIndex
        }
    }

    private fun initChart(friendNames: ArrayList<String>, friendPoints: ArrayList<Int>,
                          rowView: View, user: Friend, userIndex: Int) {
        val chart = rowView.findViewById(R.id.friend_points_bar_chart) as BarChart
        chart.setDescription("Your points among friends'.")
        chart.legend.isEnabled = false
        chart.data = createBarData(friendNames, friendPoints, user, userIndex)
        chart.animateXY(900, 900)
        chart.setDescription("")
    }

    private fun createBarData(friendNames: ArrayList<String>, friendPoints: ArrayList<Int>,
                              user: Friend, userIndex: Int): BarData {
        val dataSet = createDataSet(friendNames, friendPoints, user, userIndex)
        customizeDataSet(dataSet, userIndex)
        val barData = BarData(friendNames, dataSet)
        return barData
    }

    private fun createDataSet(friendNames: ArrayList<String>, friendPoints: ArrayList<Int>,
                              user: Friend, userIndex: Int): BarDataSet {
        friendNames.add(userIndex, "You")
        friendPoints.add(userIndex, user.points)
        val dataSet = BarDataSet(friendNames.mapIndexed { index, name ->
            BarEntry(friendPoints[index].toFloat(), index, name)
        }, "")
        return dataSet
    }

    private fun customizeDataSet(dataSet: BarDataSet, userIndex: Int) {
        dataSet.barSpacePercent = 10f
        val colors = ArrayList(ColorTemplate.DarkChartColors)
        colors.add(userIndex, ColorTemplate.Black)
        dataSet.colors = colors
    }

}