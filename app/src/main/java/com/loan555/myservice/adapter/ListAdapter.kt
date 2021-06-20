package com.loan555.myservice.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.loan555.myservice.R
import com.loan555.myservice.model.Audio
import com.loan555.myservice.model.AudioList

const val TAG = "tag_debug"

class RecyclerSongAdapter(var listData: AudioList, val listener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerSongAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textViewName: TextView = itemView.findViewById(R.id.nameSong)
        var textViewSinger: TextView = itemView.findViewById(R.id.content)
        var btnMore: ImageButton = itemView.findViewById(R.id.more_event)
        val img: ImageView = itemView.findViewById(R.id.img)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClick(v, listData.getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_song, parent, false)
    )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.img.setImageBitmap(listData.getItem(position).bitmap)
        holder.textViewName.text = listData.getItem(position).title
        holder.textViewSinger.text = listData.getItem(position).artists
        holder.btnMore.setOnClickListener {
            Log.d(TAG, "detail for: ${listData.getItem(position)}")
        }
    }

    override fun getItemCount(): Int = listData.get().size

    interface OnItemClickListener {
        fun onItemClick(v: View?, item: Audio)
    }
}