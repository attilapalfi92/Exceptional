package com.attilapalfi.exceptional.ui.main.friends_page

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
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.ImageCache
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by palfi on 2015-08-23.
 */
class FriendsFragment : Fragment(), FriendChangeListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var imageCache: ImageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.INSTANCE.applicationComponent.inject(this)
        friendStore.addFriendChangeListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFriendAdapter()
        val fragmentView = initRecyclerView(inflater, container)
        onFriendsChanged()
        return fragmentView
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        friendStore.removeFriendChangeListener(this)
        super.onDestroy()
    }

    private fun initFriendAdapter() {
        val values = friendStore.getStoredFriends()
        friendAdapter = FriendAdapter(values, WeakReference(activity), imageCache)
    }

    private fun initRecyclerView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        val fragmentView = inflater!!.inflate(R.layout.fragment_friends, container, false)
        recyclerView = fragmentView!!.findViewById(R.id.friend_recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = friendAdapter
        friendAdapter.setRecyclerView(recyclerView)
        return fragmentView
    }

    override fun onFriendsChanged() {
        friendAdapter.setValues(friendStore.getStoredFriends())
        friendAdapter.notifyDataSetChanged()
    }
}
