//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.panruijie.exoplayer.base.adapter.animation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class Utils {
    private static final float DENSITY;
    private static final Canvas sCanvas;

    private Utils() {
    }

    public static int dp2Px(int dp) {
        return Math.round((float)dp * DENSITY);
    }

    public static Bitmap createBitmapFromView(View view) {
        if(view instanceof ImageView) {
            Drawable drawable = ((ImageView)view).getDrawable();
            if(drawable != null && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable)drawable).getBitmap();
            }
        }

        view.clearFocus();
        Bitmap bitmap = createBitmapSafely(view.getWidth(), view.getHeight(), Config.ARGB_8888, 1);
        if(bitmap != null) {
            Canvas var2 = sCanvas;
            synchronized(sCanvas) {
                Canvas canvas = sCanvas;
                canvas.setBitmap(bitmap);
                view.draw(canvas);
                canvas.setBitmap((Bitmap)null);
            }
        }

        return bitmap;
    }

    public static Bitmap createBitmapSafely(int width, int height, Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError var5) {
            var5.printStackTrace();
            if(retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            } else {
                return null;
            }
        }
    }

    static {
        DENSITY = Resources.getSystem().getDisplayMetrics().density;
        sCanvas = new Canvas();
    }
}
