package com.attilapalfi.exceptional.ui.main.main_page

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.FirstStartFinishedListener
import com.attilapalfi.exceptional.interfaces.PointChangeListener
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.persistence.ImageCache
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.StatSupplier
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import javax.inject.Inject

/**
 */
class MainFragment : Fragment(), FirstStartFinishedListener, PointChangeListener {
    private var myView: View? = null
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var typeStore: ExceptionTypeStore
    @Inject
    lateinit var statSupplier: StatSupplier
    private var globalTypePieData = PieData(listOf(), PieDataSet(listOf(), ""))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        metadataStore.addFirstStartFinishedListener(this)
        metadataStore.addPointChangeListener(this)
        myView = inflater!!.inflate(R.layout.fragment_main, container, false)
        initCharts()
        return myView
    }

    override fun onResume() {
        super.onResume()
        setViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        metadataStore.removeFirstStartFinishedListener(this)
        metadataStore.removePointChangeListener(this)
    }

    private fun initCharts() {
        async {
            if (globalTypePieData.dataSet.entryCount == 0) {
                try {
                    val throwCounts = statSupplier.globalThrowCounts
                    val pieDataSet = PieDataSet(throwCounts.values.mapIndexed { index, count -> Entry(count.toFloat(), index) }, "Types")
                    setDataSetColors(pieDataSet)
                    globalTypePieData = PieData(throwCounts.keys.map { typeStore.findById(it).shortName }, pieDataSet)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            uiThread {
                val globalTypeChart = myView!!.findViewById(R.id.global_type_chart) as PieChart
                globalTypeChart.setDescription("Count of different exception types thrown globally.")
                globalTypeChart.data = globalTypePieData
                globalTypeChart.invalidate()
            }
        }
    }

    private fun setDataSetColors(dataSet: DataSet<Entry>) {
        dataSet.colors = listOf(
                resources.getColor(R.color.purple_200),
                resources.getColor(R.color.indigo_200),
                resources.getColor(R.color.blue_200),
                resources.getColor(R.color.teal_200),
                resources.getColor(R.color.green_200),
                resources.getColor(R.color.lime_200),
                resources.getColor(R.color.yellow_200),
                resources.getColor(R.color.orange_200),
                resources.getColor(R.color.grey_200),

                resources.getColor(R.color.purple_400),
                resources.getColor(R.color.indigo_400),
                resources.getColor(R.color.blue_400),
                resources.getColor(R.color.teal_400),
                resources.getColor(R.color.green_400),
                resources.getColor(R.color.lime_400),
                resources.getColor(R.color.yellow_400),
                resources.getColor(R.color.orange_400),
                resources.getColor(R.color.grey_400),

                resources.getColor(R.color.purple_600),
                resources.getColor(R.color.indigo_600),
                resources.getColor(R.color.blue_600),
                resources.getColor(R.color.teal_600),
                resources.getColor(R.color.green_600),
                resources.getColor(R.color.lime_600),
                resources.getColor(R.color.yellow_600),
                resources.getColor(R.color.orange_600),
                resources.getColor(R.color.grey_600)
        )
    }

    private fun setCharts() {
        val globalTypeChart = myView!!.findViewById(R.id.global_type_chart) as PieChart

        val pieDataSet = PieDataSet(listOf(Entry(1f, 1, "Data 1"), Entry(2f, 2, "Data 2"),
                Entry(3f, 3, "Data 3"), Entry(4f, 4, "Data 4")), "")

        pieDataSet.colors = listOf(resources.getColor(R.color.exceptional_blue),
                resources.getColor(R.color.exceptional_green),
                resources.getColor(R.color.exceptional_red),
                resources.getColor(R.color.exceptional_purple),
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.orange))

        val pieData = PieData(listOf("Type 1", "Type 2", "Type 3", "Type 4"), pieDataSet)

        globalTypeChart.data = pieData
    }

    private fun setViews() {
        setImageView()
        setNameView()
        setPointView()
    }

    private fun setImageView() {
        val imageView = myView!!.findViewById(R.id.myMainImageView) as ImageView
        imageCache.setImageToView(metadataStore.user, imageView)
    }

    private fun setNameView() {
        val nameView = myView!!.findViewById(R.id.mainNameTextView) as TextView
        val nameText = resources.getString(R.string.main_welcome_before_name) + " " + metadataStore.user.firstName.trim { it <= ' ' } + "!"
        nameView.text = nameText
    }

    private fun setPointView() {
        val pointView = myView!!.findViewById(R.id.mainPointTextView) as TextView
        val pointText = getString(R.string.main_point_view_pre) + " " + metadataStore.getPoints() + " " + getString(R.string.main_point_view_post)
        pointView.text = pointText
    }

    override fun onFirstStartFinished(state: Boolean) {
        if (state) {
            setViews()
        }
    }

    override fun onPointsChanged() {
        setPointView()
    }
}
