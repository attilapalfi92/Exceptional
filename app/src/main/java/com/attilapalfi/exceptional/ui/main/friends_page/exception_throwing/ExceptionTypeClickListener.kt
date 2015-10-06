package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Switch
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.model.ExceptionFactory
import com.attilapalfi.exceptional.model.ExceptionType
import com.attilapalfi.exceptional.model.Question
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.ExceptionService
import com.attilapalfi.exceptional.services.LocationException
import com.attilapalfi.exceptional.services.LocationProvider
import com.attilapalfi.exceptional.ui.main.Constants
import com.attilapalfi.exceptional.ui.main.MainActivity
import java.math.BigInteger
import javax.inject.Inject

/**
 * Created by palfi on 2015-10-02.
 */
public class ExceptionTypeClickListener(private val values: List<ExceptionType>,
                                        private val recyclerView: RecyclerView,
                                        private val activity: Activity) : View.OnClickListener {
    private var switchView: Switch? = null
    private var questionView: EditText? = null
    private var noRadioView: RadioButton? = null
    private var yesRadioView: RadioButton? = null
    private var questionText = ""
    @Inject lateinit val locationProvider: LocationProvider
    @Inject lateinit val exceptionService: ExceptionService
    @Inject lateinit val exceptionFactory: ExceptionFactory
    @Inject lateinit val metadataStore: MetadataStore

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
    }

    override fun onClick(view: View) {
        try {
            val location = locationProvider.location
            val exception = createException(view, location)
            buildDoubleNothingDialog(exception)
        } catch (e: LocationException) {
            e.printStackTrace()
        }

    }

    private fun buildDoubleNothingDialog(exception: com.attilapalfi.exceptional.model.Exception) {
        val builder = initDialogBuilder(exception)
        setCallbacks(builder, exception)
        val dialog = builder.build()
        setClickListeners(dialog)
        dialog.show()
    }

    private fun initDialogBuilder(exception: Exception): MaterialDialog.Builder {
        return MaterialDialog.Builder(activity)
                .title(activity.getString(R.string.throw_question) + " " + exception.shortName + "?")
                .customView(R.layout.throw_layout, true)
                .positiveText(R.string.throwException)
                .negativeText(R.string.cancel)
    }

    private fun setCallbacks(builder: MaterialDialog.Builder, exception: Exception) {
        builder.callback(object : MaterialDialog.ButtonCallback() {
            override fun onPositive(dialog: MaterialDialog?) {
                if (inputIsValid()) {
                    exceptionService.throwException(exception, getQuestion())
                    navigateToMainPage()
                }
            }

            override fun onNegative(dialog: MaterialDialog?) {
            }
        })
    }

    private fun inputIsValid(): Boolean {
        if ( switchView?.isChecked == true ) {
            questionText = questionView?.text.toString().trim()
            if (isLengthInvalid()) return false
            if (isNotWellFormatted()) return false
        }
        return true
    }

    private fun getQuestion(): Question {
        return Question(
                questionText,
                yesRadioView?.isChecked == true,
                switchView?.isChecked == true,
                false
        )
    }

    private fun isNotWellFormatted(): Boolean {
        if ( !questionText.endsWith('?') ) {
            Toast.makeText(activity,  R.string.question_ends_question , Toast.LENGTH_SHORT).show();
            return true
        }
        return false
    }

    private fun isLengthInvalid(): Boolean {
        if ( questionText.length() < 3 ) {
            Toast.makeText(activity, R.string.too_short_question, Toast.LENGTH_SHORT).show();
            return true
        } else if ( questionText.length() > 100 ) {
            Toast.makeText(activity, R.string.too_long_question, Toast.LENGTH_SHORT).show();
            return true
        }
        return false
    }

    private fun setClickListeners(dialog: MaterialDialog) {
        val throwView = dialog.customView
        throwView?.let {
            switchView = throwView.findViewById(R.id.double_or_nothing_switch) as Switch
            switchView?.setOnClickListener({ switchListener(it) })
            noRadioView = throwView.findViewById(R.id.double_or_nothing_no_radio) as RadioButton
            noRadioView?.setOnClickListener({ radioNoListener(it) })
            yesRadioView = throwView.findViewById(R.id.double_or_nothing_yes_radio) as RadioButton
            yesRadioView?.setOnClickListener({ radioYesListener(it) })
            questionView = throwView.findViewById(R.id.double_or_nothing_question_edit_text) as EditText
        }
    }

    private fun switchListener(view: View) {
        if (switchView!!.isChecked) {
            setViewsEnabled(true)
        } else {
            setViewsEnabled(false)
        }
    }

    private fun setViewsEnabled(state: Boolean) {
        noRadioView?.isEnabled = state
        yesRadioView?.isEnabled = state
        questionView?.isEnabled = state
    }

    private fun radioNoListener(view: View) {
        if (noRadioView?.isChecked == true) {
            yesRadioView?.isChecked = false
        }
    }

    private fun radioYesListener(view: View) {
        if (yesRadioView?.isChecked == true) {
            noRadioView?.isChecked = false
        }
    }

    private fun createException(view: View, location: Location): Exception {
        val itemPosition = recyclerView.getChildAdapterPosition(view)
        val exception = createExceptionFromPosition(itemPosition)
        exception.latitude = location.latitude
        exception.longitude = location.longitude
        return exception
    }

    private fun createExceptionFromPosition(itemPosition: Int): Exception {
        val exceptionType = values.get(itemPosition)
        val friendId = BigInteger(activity.intent.getStringExtra(Constants.FRIEND_ID))
        return exceptionFactory.createExceptionWithType(
                exceptionType,
                metadataStore.user.id,
                friendId)
    }

    private fun navigateToMainPage() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
        activity.finish()
    }
}
