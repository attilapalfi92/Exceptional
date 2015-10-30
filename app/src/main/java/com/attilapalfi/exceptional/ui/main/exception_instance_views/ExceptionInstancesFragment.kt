package com.attilapalfi.exceptional.ui.main.exception_instance_views

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.ExceptionInstanceStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.ExceptionRestConnector
import com.attilapalfi.exceptional.ui.main.friends_page.FriendDetailsActivity
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-08-24.
 */
class ExceptionInstancesFragment : Fragment(), ExceptionRefreshListener, ExceptionChangeListener,
        SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var exceptionRestConnector: ExceptionRestConnector
    @Inject
    lateinit var exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit var metadataStore: MetadataStore
    private var friend: Friend? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScaleInAnimationAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var refreshing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.INSTANCE.applicationComponent.inject(this)
        exceptionInstanceStore.addExceptionChangeListener(this)
        val activity: FriendDetailsActivity
        if (getActivity() is FriendDetailsActivity) {
            activity = getActivity() as FriendDetailsActivity
            friend = activity.friend
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = initRecyclerView(inflater, container)
        initSwipeLayout(view)
        return view
    }

    private fun initSwipeLayout(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.exception_swipe_container) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE)
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.exceptional_blue),
                ContextCompat.getColor(context, R.color.exceptional_green),
                ContextCompat.getColor(context, R.color.exceptional_red),
                ContextCompat.getColor(context, R.color.exceptional_purple)
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onExceptionsChanged()
    }

    override fun onDestroy() {
        exceptionInstanceStore.removeExceptionChangeListener(this)
        super.onDestroy()
    }

    override fun onExceptionsChanged() {
        (adapter.wrappedAdapter as ExceptionInstanceAdapter).setValues(generateValues())
        (adapter.wrappedAdapter as ExceptionInstanceAdapter).notifyDataSetChanged()
    }

    override fun onRefresh() {
        if (!refreshing) {
            refreshing = true
            val currentTime = System.currentTimeMillis()
            if (currentTime > lastSyncTime + 10000) {
                lastSyncTime = currentTime
            }
            actualRefresh()
        }
    }

    private fun actualRefresh() {
        if (metadataStore.loggedIn) {
            exceptionRestConnector.refreshExceptions(this)
        } else {
            Toast.makeText(activity.applicationContext, R.string.login_first, Toast.LENGTH_SHORT).show()
            onExceptionRefreshFinished()
        }
    }

    override fun onExceptionRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
        refreshing = false
    }


    private fun initRecyclerView(inflater: LayoutInflater?, container: ViewGroup?): View {
        val fragmentView = inflater!!.inflate(R.layout.fragment_exception_instances, container, false)
        recyclerView = fragmentView.findViewById(R.id.exception_recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = getAdapter(recyclerView)
        return fragmentView
    }

    private fun getAdapter(recyclerView: RecyclerView): ScaleInAnimationAdapter {
        val values = generateValues()
        adapter = ScaleInAnimationAdapter(ExceptionInstanceAdapter(values, recyclerView))
        adapter.notifyDataSetChanged()
        adapter.setDuration(200)
        adapter.setFirstOnly(true)
        return adapter
    }

    private fun generateValues(): List<Exception> {
        val allValues = exceptionInstanceStore.getExceptionList()
        if (friend != null) {
            val filteredValues = ArrayList<Exception>()
            for (exception in allValues) {
                if (exception.fromWho == friend!!.id || exception.toWho == friend!!.id) {
                    filteredValues.add(exception)
                }
            }
            return filteredValues
        } else {
            return allValues
        }
    }

    companion object {
        private var lastSyncTime: Long = 0
    }
}
