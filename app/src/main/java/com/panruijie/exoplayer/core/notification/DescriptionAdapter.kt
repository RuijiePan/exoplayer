package com.panruijie.exoplayer.core.notification

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.panruijie.App
import com.panruijie.exoplayer.MainActivity
import com.panruijie.exoplayer.R


/**
 * Created by panruijie on 2019/2/2.
 **/
class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun createCurrentContentIntent(player: Player?): PendingIntent? {
        val window = player?.getCurrentWindowIndex()
        return null
    }

    override fun getCurrentContentText(player: Player?): String? {
        val window = player?.getCurrentWindowIndex()
        val pos = player?.currentPosition
        return "position = " + pos
    }

    override fun getCurrentContentTitle(player: Player?): String {
        val window = player?.getCurrentWindowIndex()
        return "pos"
    }

    override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? {
        val window = player?.getCurrentWindowIndex()
        val bitmap = BitmapFactory.decodeResource(App.getContext().resources, R.mipmap.ic_launcher)
        callback?.onBitmap(bitmap)
        return bitmap
    }

}