package com.dteviot.simpleComicBookViewer;

import android.view.MotionEvent;

/*
 * React to user performing flings on a Bitmap View
 */
public interface IBitmapViewController {
    /*
     * Called when user does a fling
     */
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);

    /*
     * Called when user does a long press
     */
    public void onLongPress();
}
