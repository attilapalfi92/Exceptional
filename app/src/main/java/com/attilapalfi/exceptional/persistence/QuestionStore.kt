package com.attilapalfi.exceptional.persistence

import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.ExceptionQuestion
import com.attilapalfi.exceptional.ui.question_views.QuestionChangeListener
import io.paperdb.Book
import io.paperdb.Paper
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class QuestionStore {
    companion object {
        private val KOTLIN_DATABASE = "KOTLIN_DATABASE"
        private val QUESTION_LIST = "QUESTION_LIST"
        private val EMPTY_QUESTION_LIST = ArrayList<ExceptionQuestion>()
    }

    private val changeListeners = HashSet<QuestionChangeListener>()
    private val database: Book
    private val storedQuestions = Collections.synchronizedList(ArrayList<ExceptionQuestion>())
    @Inject
    lateinit val friendStore: FriendStore
    private val handler = Handler(Looper.getMainLooper())

    public constructor() {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(KOTLIN_DATABASE)
        storedQuestions.addAll(database.read(QUESTION_LIST, EMPTY_QUESTION_LIST))
    }

    public fun hasQuestions() = storedQuestions.isNotEmpty()

    public fun addQuestion(question: ExceptionQuestion) {
        addToStore(question)
        database.write(QUESTION_LIST, storedQuestions)
        notifyListeners()
    }

    public fun addQuestionList(questions: List<ExceptionQuestion>) {
        if (questions.isNotEmpty()) {
            synchronized(storedQuestions) {
                questions.forEach { addToStore(it) }
            }
            database.write(QUESTION_LIST, storedQuestions)
            notifyListeners()
        }
    }

    private fun addToStore(question: ExceptionQuestion) {
        synchronized(storedQuestions) {
            var index = Collections.binarySearch(storedQuestions, question)
            if (index < 0) {
                index = -index - 1
                storedQuestions.add(index, question)
            }
        }
    }

    public fun getQuestions() = ArrayList(storedQuestions)

    public fun addQuestionListAsync(questionExceptions: List<ExceptionQuestion>) {
        Thread({
            addQuestionList(questionExceptions)
            notifyListeners()
        }).start()
    }

    private fun notifyListeners() {
        if ( Looper.myLooper() === Looper.getMainLooper() ) {
            changeListeners.forEach { it.onQuestionsChanged() }
        } else {
            handler.post { changeListeners.forEach { it.onQuestionsChanged() } }
        }
    }

    public fun addChangeListener(questionChangeListener: QuestionChangeListener)
            = changeListeners.add(questionChangeListener)

    public fun removeChangeListener(questionChangeListener: QuestionChangeListener)
            = changeListeners.remove(questionChangeListener)
}