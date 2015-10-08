package com.attilapalfi.exceptional.ui.question_views

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.persistence.QuestionChangeListener
import com.attilapalfi.exceptional.persistence.QuestionStore
import com.attilapalfi.exceptional.ui.main.MainActivity
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class QuestionsFragment : Fragment(), QuestionChangeListener {
    private var exceptionQuestions: List<Exception> = ArrayList()
    private var recyclerView: RecyclerView? = null
    private var questionAdapter: QuestionAdapter? = null
    @Inject
    lateinit val questionStore: QuestionStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        initList()
    }

    private fun initList() {
        exceptionQuestions = questionStore.getQuestions()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = iniRecyclerView(inflater, container)
        questionStore.addChangeListener(this)
        questionAdapter?.notifyDataSetChanged()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        questionStore.removeChangeListener(this)
    }

    private fun iniRecyclerView(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.fragment_questions, container, false)
        recyclerView = view.findViewById(R.id.question_recycler_view) as RecyclerView
        recyclerView?.let {
            it.layoutManager = LinearLayoutManager(activity)
            questionAdapter = QuestionAdapter(exceptionQuestions)
            it.adapter = questionAdapter
        }
        return view
    }

    override public fun onQuestionsChanged() {
        questionAdapter?.let {
            it.values = questionStore.getQuestions()
            if ( it.values.isEmpty() ) {
                val intent = Intent(activity, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                activity.startActivity(intent)
            } else {
                it.notifyDataSetChanged()
            }
        }
    }
}