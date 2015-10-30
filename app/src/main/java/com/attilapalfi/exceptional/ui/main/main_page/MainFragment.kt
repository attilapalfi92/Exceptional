package com.attilapalfi.exceptional.ui.main.main_page

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.FriendChangeListener
import com.attilapalfi.exceptional.interfaces.GlobalThrowCountChangeListener
import com.attilapalfi.exceptional.interfaces.PointChangeListener
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.ImageCache
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.StatSupplier
import com.attilapalfi.exceptional.ui.main.main_page.recycler_adapter.MainAdapter
import com.attilapalfi.exceptional.ui.main.main_page.recycler_model.FriendPointsChartModel
import com.attilapalfi.exceptional.ui.main.main_page.recycler_model.TypeThrowChartModel
import com.attilapalfi.exceptional.ui.main.main_page.recycler_model.UserRowModel
import javax.inject.Inject

/**
 */
class MainFragment : Fragment(), PointChangeListener, FriendChangeListener, GlobalThrowCountChangeListener {
    private var myView: View? = null
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var imageCache: ImageCache
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var typeStore: ExceptionTypeStore
    @Inject
    lateinit var statSupplier: StatSupplier

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        metadataStore.addPointChangeListener(this)
        friendStore.addFriendChangeListener(this)
        statSupplier.addThrowCountListener(this)
        myView = initViewAndModel(inflater, container)
        return myView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        metadataStore.removePointChangeListener(this)
        friendStore.removeFriendChangeListener(this)
        statSupplier.removeThrowCountListener(this)
    }

    private fun initViewAndModel(inflater: LayoutInflater?, container: ViewGroup?): View? {
        if (inflater != null && container != null) {
            val fragmentView = initViewAndRecycler(container, inflater)
            initAdapter()
            return fragmentView
        }
        return null
    }

    private fun initViewAndRecycler(container: ViewGroup?, inflater: LayoutInflater): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = fragmentView.findViewById(R.id.main_recycler_view) as RecyclerView;
        recyclerView.layoutManager = LinearLayoutManager(activity)
        return fragmentView
    }

    private fun initAdapter() {
        val adapterData = listOf(UserRowModel(metadataStore.user), FriendPointsChartModel(), TypeThrowChartModel())
        adapter = MainAdapter(adapterData, recyclerView)
        recyclerView.adapter = adapter
    }

    override fun onPointsChanged() {
        adapter.notifyItemChanged(0, 1)
    }

    override fun onFriendsChanged() {
        adapter.notifyItemRangeChanged(0, 1)
    }

    override fun onGlobalThrowCountChanged() {
        adapter.notifyItemChanged(2)
    }
}
