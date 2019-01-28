package com.panruijie.exoplayer.util

import android.graphics.Matrix
import android.graphics.RectF
import android.view.TextureView

/**
 * Created by panruijie on 2019/1/28.
 **/
object TextureViewHelper {

    @JvmStatic
    /** Applies a texture rotation to a [TextureView].  */
    fun applyTextureViewRotation(textureView: TextureView?, textureViewRotation: Int) {
        if (textureView == null) {
            return
        }
        val textureViewWidth = textureView.width.toFloat()
        val textureViewHeight = textureView.height.toFloat()
        if (textureViewWidth == 0f || textureViewHeight == 0f || textureViewRotation == 0) {
            textureView.setTransform(null)
        } else {
            val transformMatrix = Matrix()
            val pivotX = textureViewWidth / 2
            val pivotY = textureViewHeight / 2
            transformMatrix.postRotate(textureViewRotation.toFloat(), pivotX, pivotY)

            // After rotation, scale the rotated texture to fit the TextureView size.
            val originalTextureRect = RectF(0f, 0f, textureViewWidth, textureViewHeight)
            val rotatedTextureRect = RectF()
            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect)
            transformMatrix.postScale(
                    textureViewWidth / rotatedTextureRect.width(),
                    textureViewHeight / rotatedTextureRect.height(),
                    pivotX,
                    pivotY)
            textureView.setTransform(transformMatrix)
        }
    }
}