package ru.otvinta.pentamail

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class FolderDataAdapter(context: Context, var folders: List<Folder>) :
    RecyclerView.Adapter<FolderDataAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderDataAdapter.ViewHolder {

        val view = inflater.inflate(R.layout.folder_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderDataAdapter.ViewHolder, position: Int) {
        val folder = folders[position]
        holder.titleView.text = folder.title
        holder.idView.text = folder.id.toString()
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val titleView: TextView = view.findViewById(R.id.folderName) as TextView
        internal val idView: TextView = view.findViewById(R.id.folderId) as TextView
    }
}