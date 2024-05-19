package com.huikka.supertag.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.huikka.supertag.CustomTimer
import com.huikka.supertag.R

class ChaserTimers : Fragment(R.layout.chaser_timers) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chaser_timers, container, false)

        val customItemView = view.findViewById<CustomTimer>(R.id.testTimer)
        customItemView.setOnClickListener {
            onItemClick("timer")
            // Handle click event here if needed
        }

        return view
    }

    private fun onItemClick(text: String) {
        Toast.makeText(requireContext(), "Clicked: $text", Toast.LENGTH_SHORT).show()
    }

}