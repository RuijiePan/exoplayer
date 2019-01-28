package com.panruijie.exoplayer.util

import android.support.annotation.Size
import com.google.android.exoplayer2.Player

/**
 * Created by panruijie on 2019/1/22..
 * 用于保存onPlayerStateChanged的历史状态
 **/
class StateStore {

    companion object {
        val FLAG_PLAY_WHEN_READY = 0xF0000000.toInt()
        val STATE_SEEKING = 100
    }

    //We keep the last few states because that is all we need currently
    private val prevStates = intArrayOf(Player.STATE_IDLE, Player.STATE_IDLE, Player.STATE_IDLE, Player.STATE_IDLE)

    fun resetState() {
        for (i in prevStates.indices) {
            prevStates[i] = Player.STATE_IDLE
        }
    }

    fun setMostRecentState(playWhenReady: Boolean, state: Int) {
        val newState = getState(playWhenReady, state)
        if (prevStates[3] == newState) {
            return
        }

        prevStates[0] = prevStates[1]
        prevStates[1] = prevStates[2]
        prevStates[2] = prevStates[3]
        prevStates[3] = state
    }

    fun getState(playWhenReady: Boolean, state: Int): Int {
        return state or if (playWhenReady) FLAG_PLAY_WHEN_READY else 0
    }

    fun getMostRecentState(): Int {
        return prevStates[3]
    }

    fun isLastReportedPlayWhenReady(): Boolean {
        return prevStates[3] and FLAG_PLAY_WHEN_READY != 0
    }

    fun matchesHistory(@Size(min = 1, max = 4) states: IntArray, ignorePlayWhenReady: Boolean): Boolean {
        var flag = true
        val andFlag = if (ignorePlayWhenReady) FLAG_PLAY_WHEN_READY.inv() else 0x0.inv()
        val startIndex = prevStates.size - states.size

        for (i in startIndex until prevStates.size) {
            flag = flag and (prevStates[i] and andFlag == states[i - startIndex] and andFlag)
        }

        return flag
    }
}