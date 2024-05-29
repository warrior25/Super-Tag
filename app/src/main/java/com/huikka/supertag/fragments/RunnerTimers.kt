package com.huikka.supertag.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.huikka.supertag.CustomTimer
import com.huikka.supertag.R

class RunnerTimers : Fragment(R.layout.runner_timers) {

    lateinit var moneyTimer: CustomTimer
    lateinit var zoneTimer: CustomTimer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.runner_timers, container, false)

        moneyTimer = view.findViewById(R.id.runnerMoneyTimer)
        zoneTimer = view.findViewById(R.id.zoneTimer)

        return view
    }

}