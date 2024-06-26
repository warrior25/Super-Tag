package com.huikka.supertag.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.huikka.supertag.CustomTimer
import com.huikka.supertag.R

class ChaserTimers : Fragment(R.layout.chaser_timers) {

    lateinit var runnerLocationTimer: CustomTimer
    lateinit var moneyTimer: CustomTimer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chaser_timers, container, false)

        runnerLocationTimer = view.findViewById(R.id.runnerLocationTimer)
        moneyTimer = view.findViewById(R.id.chaserMoneyTimer)

        return view
    }
}