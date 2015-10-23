package com.attilapalfi.exceptional.ui.question_views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.persistence.QuestionStore
import javax.inject.Inject

public class AnswerExceptionActivity : AppCompatActivity() {
    @Inject
    lateinit var questionStore: QuestionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_exception)
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
