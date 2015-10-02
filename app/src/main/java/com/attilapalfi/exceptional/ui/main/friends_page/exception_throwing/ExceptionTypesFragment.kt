package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import java.util.ArrayList

import javax.inject.Inject

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.ExceptionType
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore

public class ExceptionTypesFragment : Fragment() {
    public var position: Int = 0
    public var typeName = ""
    private var exceptionTypes: List<ExceptionType> = ArrayList<ExceptionType>()
    private var recyclerView: RecyclerView? = null
    private var typeAdapter: ExceptionTypeAdapter? = null
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        initExceptionTypes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun initExceptionTypes() {
        val index = instanceCounter++ % exceptionTypeStore.exceptionTypes.size()
        val types = ArrayList(exceptionTypeStore.exceptionTypes)
        typeName = types.get(index)
        exceptionTypes = exceptionTypeStore.getExceptionTypeListByName(typeName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = iniRecyclerView(inflater, container)
        typeAdapter?.notifyDataSetChanged()
        return view
    }

    private fun iniRecyclerView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.fragment_exception_types, container, false)
        recyclerView = view.findViewById(R.id.exception_type_recycler_view) as RecyclerView
        recyclerView?.let {
            it.layoutManager = LinearLayoutManager(activity)
            typeAdapter = ExceptionTypeAdapter(activity, it, exceptionTypes)
            it.adapter = typeAdapter
        }
        return view
    }

    companion object {
        private var instanceCounter = 0
    }
}
