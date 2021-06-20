package com.loan555.myservice.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.myservice.R
import com.loan555.myservice.adapter.RecyclerSongAdapter
import com.loan555.myservice.currentFragment
import com.loan555.myservice.model.Audio
import com.loan555.myservice.model.AudioList
import kotlinx.android.synthetic.main.fragment_songs_list.*

class ListSongFragment(private val audioList: AudioList, private val listener: ClickSongListener) : Fragment(),
    RecyclerSongAdapter.OnItemClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_songs_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentFragment = "home"
        list.layoutManager =
            LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = RecyclerSongAdapter(audioList, this)
        list.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onItemClick(v: View?, item: Audio) {
        listener.click(item)
    }

    interface ClickSongListener{
        fun click(item: Audio)
    }
}