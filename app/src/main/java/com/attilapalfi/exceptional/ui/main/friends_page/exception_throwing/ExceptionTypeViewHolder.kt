package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.model.ExceptionType

/**
 * Created by palfi on 2015-10-02.
 */
public class ExceptionTypeViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
    private val shortNameView: TextView
    private val fullNameView: TextView
    private val descriptionView: TextView
    private val submitterView: TextView

    init {
        shortNameView = itemView.findViewById(R.id.type_short_name_text) as TextView
        fullNameView = itemView.findViewById(R.id.type_full_name_text) as TextView
        descriptionView = itemView.findViewById(R.id.type_description_text) as TextView
        submitterView = itemView.findViewById(R.id.type_submitter_text) as TextView
    }

    public fun bindRow(exceptionType: ExceptionType) {
        shortNameView.text = exceptionType.shortName
        val fullName = getFullName(exceptionType)
        fullNameView.text = fullName
        descriptionView.text = exceptionType.description
        bindSubmitter(exceptionType)
    }

    private fun bindSubmitter(exceptionType: ExceptionType) {
        val submitter = exceptionType.submitter
        var submitterString = context.resources.getString(R.string.submitter_text)
        if (submitter != null) {
            submitterString += submitter.firstName + " " + submitter.lastName
        } else {
            submitterString += "System"
        }
        submitterView.text = submitterString
    }

    private fun getFullName(exceptionType: ExceptionType): String {
        val fullNameParts = exceptionType.fullName().split("\\.")
        var fullName = ""
        for (part in fullNameParts) {
            fullName += part + "." + "\n"
        }
        fullName = fullName.substring(0, fullName.length - 2)
        return fullName
    }
}
