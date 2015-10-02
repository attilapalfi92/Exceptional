package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-08-30.
 */
public class ExceptionTypePagerAdapter(fragmentManager: FragmentManager, private val activity: Activity) :
        FragmentPagerAdapter(fragmentManager), ViewPager.OnPageChangeListener {

    private val exceptionTypes = ArrayList<String>()
    public var exceptionTypeStore: ExceptionTypeStore? = null
        @Inject
        public set
        public get

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        exceptionTypes.addAll(exceptionTypeStore?.exceptionTypes)
    }

    override fun getItem(position: Int): Fragment {
        return ExceptionTypesFragment()
    }

    override fun getCount(): Int {
        return exceptionTypes.size()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        if (position < exceptionTypes.size()) {
            activity.title = exceptionTypes.get(position)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}
