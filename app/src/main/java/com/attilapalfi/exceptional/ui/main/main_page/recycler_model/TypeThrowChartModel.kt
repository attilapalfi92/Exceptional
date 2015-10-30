package com.attilapalfi.exceptional.ui.main.main_page.recycler_model

import android.view.View
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.rest.StatSupplier
import com.attilapalfi.exceptional.ui.main.main_page.ColorTemplate
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-28.
 */
public class TypeThrowChartModel : RowItemModel() {
    override val rowType = RowType.TYPES_PIE_CHART
    @Inject
    lateinit var statSupplier: StatSupplier
    @Inject
    lateinit var typeStore: ExceptionTypeStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    companion object {
        val TOP_TYPES = 9
    }

    override fun bindRow(rowView: View) {
        val throwCounts = statSupplier.globalThrowCounts
        if (throwCounts.isNotEmpty()) {
            async {
                val pieData = getPieData(throwCounts)
                uiThread {
                    initChart(pieData, rowView)
                }
            }
        }
    }

    private fun getPieData(throwCounts: LinkedHashMap<Int, Long>): PieData {
        val pieDataSet = getPieDataSet(throwCounts)
        val dataList = getDataList(throwCounts)
        return PieData(dataList, pieDataSet)
    }

    private fun getPieDataSet(throwCounts: LinkedHashMap<Int, Long>): PieDataSet {
        val countList = throwCounts.values.toList()
        val otherCounts = countList.subList(TOP_TYPES, countList.size).sum()
        val entryList = getEntryList(countList, otherCounts, throwCounts.values.sum())
        return createPieDataSet(entryList)
    }

    private fun getEntryList(countList: List<Long>, otherCounts: Long, allCount: Long): ArrayList<Entry> {
        val entryList = ArrayList(countList.subList(0, TOP_TYPES)
                .mapIndexed { index, count -> Entry((count.toFloat() / allCount) * 100, index) })
        entryList.add(entryList.size, Entry((otherCounts.toFloat() / allCount) * 100, entryList.size))
        return entryList
    }

    private fun createPieDataSet(entryList: ArrayList<Entry>): PieDataSet {
        val pieDataSet = PieDataSet(entryList, "Types")
        pieDataSet.colors = ColorTemplate.ChartColors
        return pieDataSet
    }

    private fun getDataList(throwCounts: LinkedHashMap<Int, Long>): ArrayList<String> {
        val dataList = ArrayList(throwCounts.keys.toList().subList(0, TOP_TYPES).map { typeStore.findById(it).shortName })
        dataList.add(dataList.size, "others")
        return dataList
    }

    private fun initChart(pieData: PieData, rowView: View) {
        val chart = rowView.findViewById(R.id.type_throw_pie_chart) as PieChart
        chart.setDescription("Count of different exception types thrown globally.")
        chart.legend.isEnabled = false
        chart.data = pieData
        chart.setUsePercentValues(true)
        chart.animateXY(900, 900)
    }
}