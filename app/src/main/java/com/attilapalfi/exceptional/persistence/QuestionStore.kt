package com.attilapalfi.exceptional.persistence

import android.os.Handler
import android.os.Looper
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.QuestionChangeListener
import com.attilapalfi.exceptional.model.Exception
import io.paperdb.Book
import io.paperdb.Paper
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class QuestionStore : AbstractStore() {
    companion object {
        private val KOTLIN_DATABASE = "KOTLIN_DATABASE"
        private val QUESTION_LIST = "QUESTION_LIST"
        private val EMPTY_QUESTION_LIST = ArrayList<Exception>()
    }

    private val changeListeners = HashSet<QuestionChangeListener>()
    private val database: Book
    private val storedQuestions = ArrayList<Exception>()
    @Inject
    lateinit var friendStore: FriendStore
    @Inject
    lateinit var metadataStore: MetadataStore
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    private val handler = Handler(Looper.getMainLooper())
    @Volatile
    override public var initialized = false

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(KOTLIN_DATABASE)
    }

    override public fun init() {
        synchronized(storedQuestions) {
            storedQuestions.addAll(database.read(QUESTION_LIST, EMPTY_QUESTION_LIST))
            initialized = true
        }
    }

    public fun hasQuestions() = storedQuestions.isNotEmpty()

    public fun addQuestion(question: Exception) {
        addToMemoryStore(question)
        database.write(QUESTION_LIST, storedQuestions)
        notifyListeners()
    }

    public fun addUnfilteredListAsync(exceptions: List<Exception>) {
        Thread({
            waitTillInitialized()
            addUnfilteredList(exceptions)
        }).start()
    }

    public fun addUnfilteredList(exceptions: List<Exception>) {
        val questionList = exceptions.filter {
            it.question.hasQuestion &&
                    !it.question.answered &&
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
                questions.forEach { addToMemoryStore(it) }
            }
            database.write(QUESTION_LIST, storedQuestions)
            notifyListeners()
        }
    }

    private fun addToMemoryStore(question: Exception) {
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
                val removed: Exception? = storedQuestions.find { it.instanceId == instanceId }
                removed?.let { storedQuestions.remove(removed) }
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