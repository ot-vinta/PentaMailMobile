package ru.otvinta.pentamail

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView



internal class DataAdapter(context: Context, var messages: List<Message>) :
    RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {

        val view = inflater.inflate(R.layout.message_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataAdapter.ViewHolder, position: Int) {
        val message = messages[position]
        holder.senderView.text = message.sender
        holder.dateView.text = message.date
        holder.titleView.text = message.title
        holder.contentView.text = message.content
        holder.idView.text = message.id
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val senderView: TextView = view.findViewById(R.id.sender) as TextView
        internal val dateView: TextView = view.findViewById(R.id.date) as TextView
        internal val titleView: TextView = view.findViewById(R.id.title) as TextView
        internal val contentView: TextView = view.findViewById(R.id.content) as TextView
        internal val idView: TextView = view.findViewById(R.id.messageId) as TextView
    }
}