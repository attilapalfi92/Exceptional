package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import java.math.BigInteger
import java.util.ArrayList

import javax.inject.Inject

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.ui.main.Constants
import com.attilapalfi.exceptional.ui.main.page_transformers.ZoomOutPageTransformer

public class ExceptionTypeChooserActivity : AppCompatActivity() {
    private var pagerAdapter: ExceptionTypePagerAdapter? = null
    private var viewPager: ViewPager? = null
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit var friendStore: FriendStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception_type_chooser)
        Injector.INSTANCE.applicationComponent.inject(this)
        initTitle()
        initFriend()
        initViewPager()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager!!.removeOnPageChangeListener(pagerAdapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun initTitle() {
        val exceptionTypeList = ArrayList(exceptionTypeStore.exceptionTypes)
        if (exceptionTypeList.isEmpty()) {
            title = getString(R.string.no_exception_types_found)
        } else {
            title = exceptionTypeList.get(0)
        }
    }

    private fun initFriend() {
        val friendId = BigInteger(intent.getStringExtra(Constants.FRIEND_ID))
        friend = friendStore.findFriendById(friendId)
    }

    private fun initViewPager() {
        pagerAdapter = ExceptionTypePagerAdapter(supportFragmentManager, this)
        viewPager = findViewById(R.id.exception_type_pager) as ViewPager
        viewPager!!.adapter = pagerAdapter
        viewPager!!.addOnPageChangeListener(pagerAdapter)
        viewPager!!.setPageTransformer(true, ZoomOutPageTransformer())
    }

    companion object {
        public var friend: Friend? = null
    }
}
