package com.attilapalfi.exceptional.ui.main.voted_page

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.interfaces.VotedTypeListener
import com.attilapalfi.exceptional.model.ExceptionType
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.attilapalfi.exceptional.rest.VotingRestConnector
import java.util.*
import javax.inject.Inject

/**
 * Created by palfi on 2015-09-05.
 */
public class VotedExceptionsFragment : Fragment(), VotedTypeListener {
    private var adapter: VotedExceptionAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var votedTypeList: List<ExceptionType>? = null
    @Inject
    lateinit var exceptionTypeStore: ExceptionTypeStore
    @Inject
    lateinit var votingRestConnector: VotingRestConnector
    @Inject
    lateinit var metadataStore: MetadataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.INSTANCE.applicationComponent.inject(this)
        exceptionTypeStore.addVotedTypeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = initRecyclerView(inflater, container)
        adapter?.notifyDataSetChanged()
        return view
    }

    override fun onDestroy() {
        exceptionTypeStore.removeVotedTypeListener(this)
        super.onDestroy()
    }

    private fun initRecyclerView(inflater: LayoutInflater?, container: ViewGroup?): View? {
        val view = inflater?.inflate(R.layout.fragment_voted_exceptions, container, false)
        recyclerView = view?.findViewById(R.id.voted_exceptions_recycler_view) as RecyclerView
        recyclerView?.let {
            initTypeAdapter(it)
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = adapter
        }
        return view
    }

    private fun initTypeAdapter(recyclerView: RecyclerView) {
        votedTypeList = exceptionTypeStore.getVotedExceptionTypeList()
        if (votedTypeList == null) {
            votedTypeList = ArrayList<ExceptionType>()
        }
        votedTypeList?.let {
            adapter = VotedExceptionAdapter(activity, it, votingRestConnector, metadataStore, recyclerView)
        }
    }

    override fun onVoteListChanged() {
        adapter?.let {
            it.setValues(exceptionTypeStore.getVotedExceptionTypeList())
            it.notifyDataSetChanged()
        }
    }

    public class VotedExceptionAdapter(private val activity: Activity,
                                       private var values: List<ExceptionType>,
                                       private val votingRestConnector: VotingRestConnector,
                                       private val metadataStore: MetadataStore,
                                       private val recyclerView: RecyclerView
    ) : RecyclerView.Adapter<VotedExceptionAdapter.RowViewHolder>() {

        private val onClickListener = object : OnClickListener {
            override fun onClick(view: View) {
                val itemPosition = recyclerView.getChildLayoutPosition(view)
                val exceptionType = values.get(itemPosition)
                if (metadataStore.votedThisWeek) {
                    Toast.makeText(activity, R.string.you_already_voted, Toast.LENGTH_SHORT).show()
                } else {
                    showDialog(exceptionType)
                }
            }

            private fun showDialog(exceptionType: ExceptionType) {
                val data = "${exceptionType.prefix}\n${exceptionType.shortName}\n\n" +
                        "${exceptionType.description}\n\n by: ${exceptionType.submitter.fullName()}"
                MaterialDialog.Builder(activity)
                        .title(R.string.do_want_to_vote)
                        .content(data)
                        .positiveText(R.string.vote)
                        .negativeText(R.string.cancel)
                        .callback(object : MaterialDialog.ButtonCallback() {
                            override fun onPositive(dialog: MaterialDialog?) {
                                votingRestConnector.voteForType(exceptionType)
                            }
                        }).show()
            }
        }

        public fun setValues(values: List<ExceptionType>) {
            this.values = values
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.voted_exception_row_layout, parent, false)
            view.setOnClickListener(onClickListener)
            return RowViewHolder(view, activity.applicationContext)
        }

        override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
            holder.bindRow(values.get(position))
        }

        override fun getItemCount(): Int {
            return values.size
        }

        public class RowViewHolder(viewItem: View, private val context: Context) : RecyclerView.ViewHolder(viewItem) {
            private val voteCountView: TextView
            private val shortNameView: TextView
            private val fullNameView: TextView
            private val descriptionView: TextView
            private val submitterView: TextView

            init {
                voteCountView = itemView.findViewById(R.id.voted_vote_count) as TextView
                shortNameView = itemView.findViewById(R.id.voted_short_name_text) as TextView
                fullNameView = itemView.findViewById(R.id.voted_full_name_text) as TextView
                descriptionView = itemView.findViewById(R.id.voted_description_text) as TextView
                submitterView = itemView.findViewById(R.id.voted_submitter_text) as TextView
            }

            public fun bindRow(votedType: ExceptionType) {
                voteCountView.text = context.getString(R.string.voted_type_votes_text) + votedType.voteCount
                shortNameView.text = votedType.shortName
                val fullName = getFullName(votedType)
                fullNameView.text = fullName
                descriptionView.text = votedType.description
                bindSubmitter(votedType)
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

    }
}
