package com.attilapalfi.exceptional.persistence

import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.AnswerableExceptionQuestion
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.Friend
import com.attilapalfi.exceptional.model.Question
import io.paperdb.Book
import io.paperdb.Paper
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-03.
 */
public class QuestionStore {
    companion object {
        private val KOTLIN_DATABASE = "KOTLIN_DATABASE"
        private val QUESTION_LIST = "QUESTION_LIST"
        private val EMPTY_QUESTION_LIST = CopyOnWriteArrayList<AnswerableExceptionQuestion>()
    }
    private val database: Book
    private val storedQuestions = CopyOnWriteArrayList<AnswerableExceptionQuestion>()
    @Inject
    lateinit val friendStore: FriendStore

    public constructor() {
        Injector.INSTANCE.applicationComponent.inject(this)
        database = Paper.book(KOTLIN_DATABASE)
        storedQuestions.addAll( database.read(QUESTION_LIST, EMPTY_QUESTION_LIST) )
    }

    public fun hasQuestions() = storedQuestions.isEmpty()

    public fun addQuestion(question: AnswerableExceptionQuestion) {

    }
}