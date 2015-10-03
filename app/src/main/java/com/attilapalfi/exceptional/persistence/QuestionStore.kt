package com.attilapalfi.exceptional.persistence

import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.QuestionException
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
        private val EMPTY_QUESTION_LIST = ArrayList<QuestionException>()
    }

    private val database: Book
    private val storedQuestions = Collections.synchronizedList(ArrayList<QuestionException>())
    @Inject
    lateinit val friendStore: FriendStore

    public constructor() {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(KOTLIN_DATABASE)
        storedQuestions.addAll(database.read(QUESTION_LIST, EMPTY_QUESTION_LIST))
    }

    public fun hasQuestions() = storedQuestions.isNotEmpty()

    public fun addQuestion(question: QuestionException) {
        synchronized(storedQuestions) {
            addToStore(question)
        }
        database.write(QUESTION_LIST, storedQuestions)
    }

    public fun addQuestionList(questions: List<QuestionException>) {
        if (questions.isNotEmpty()) {
            synchronized(storedQuestions) {
                questions.forEach { addToStore(it) }
            }
            database.write(QUESTION_LIST, storedQuestions)
        }
    }

    private fun addToStore(question: QuestionException) {
        var index = Collections.binarySearch(storedQuestions, question)
        if (index < 0) {
            index = -index - 1
            storedQuestions.add(index, question)
        }
    }

    public fun getQuestions() = ArrayList(storedQuestions)

    fun addQuestionListAsync(questionExceptions: List<QuestionException>) {
        Thread({
            addQuestionList(questionExceptions)
        }).start()
    }
}