package com.attilapalfi.exceptional.ui.helpers

import android.app.Activity
import android.content.Intent
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.persistence.QuestionStore
import com.attilapalfi.exceptional.ui.question_views.AnswerExceptionActivity
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class QuestionNavigator {
    @Inject
    lateinit val questionStore: QuestionStore
    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    public fun navigateIfHasQuestion(activity: Activity) {
        if (questionStore.hasQuestions()) {
            val intent = Intent(activity, AnswerExceptionActivity::class.java)
            activity.startActivity(intent)
        }
    }
}