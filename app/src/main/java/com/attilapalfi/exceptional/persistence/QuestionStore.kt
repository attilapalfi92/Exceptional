package com.attilapalfi.exceptional.persistence

import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import io.paperdb.Book
import io.paperdb.Paper
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public interface QuestionChangeListener {
    fun onQuestionsChanged()
}

public class QuestionStore {
    companion object {
        private val KOTLIN_DATABASE = "KOTLIN_DATABASE"
        private val QUESTION_LIST = "QUESTION_LIST"
        private val EMPTY_QUESTION_LIST = ArrayList<Exception>()
    }

    private val changeListeners = HashSet<QuestionChangeListener>()
    private val database: Book
    private val storedQuestions = Collections.synchronizedList(ArrayList<Exception>())
    @Inject
    lateinit val friendStore: FriendStore
    @Inject
    lateinit val metadataStore: MetadataStore
    @Inject
    lateinit val exceptionTypeStore: ExceptionTypeStore
    private val handler = Handler(Looper.getMainLooper())

    public constructor() {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(KOTLIN_DATABASE)
        storedQuestions.addAll(database.read(QUESTION_LIST, EMPTY_QUESTION_LIST))
    }

    public fun hasQuestions() = storedQuestions.isNotEmpty()

    public fun addQuestion(question: Exception) {
        addToStore(question)
        database.write(QUESTION_LIST, storedQuestions)
        notifyListeners()
    }

    public fun addUnfilteredListAsync(exceptions: List<Exception>) {
        Thread({
            addUnfilteredList(exceptions)
        }).start()
    }

    public fun addUnfilteredList(exceptions: List<Exception>) {
        val questionList = exceptions.filter {
            it.question.hasQuestion &&
                    !it.question.isAnswered &&
                    metadataStore.user.id != it.fromWho
        }
        addQuestionList(questionList)
    }

    public fun addQuestionListAsync(questionExceptions: List<Exception>) {
        Thread({
            addQuestionList(questionExceptions)
        }).start()
    }

    public fun addQuestionList(questions: List<Exception>) {
        if (questions.isNotEmpty()) {
            synchronized(storedQuestions) {
                questions.forEach { addToStore(it) }
            }
            database.write(QUESTION_LIST, storedQuestions)
            notifyListeners()
        }
    }

    private fun addToStore(question: Exception) {
        synchronized(storedQuestions) {
            var index = Collections.binarySearch(storedQuestions, question)
            if (index < 0) {
                index = -index - 1
                storedQuestions.add(index, question)
            }
        }
    }

    public fun getQuestions() = ArrayList(storedQuestions)

    fun removeQuestion(instanceId: BigInteger?) {
        instanceId?.let {
            synchronized(storedQuestions) {
                val removed = storedQuestions.find { it.instanceId == instanceId }
                storedQuestions.remove(removed)
            }
            database.write(QUESTION_LIST, storedQuestions)
        }
        notifyListeners()
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