package com.dteviot.simpleComicBookViewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

/*
 * Class that shows a bitmap which user can  
 * use touch gestures to scroll, zoom, and pinch zoom.
 * A IBitmapViewController can be provided to control 
 * how to react to flings and long presses.
 * <p> 
 * The main functions of interest in this class are
 * setBitmap(), used to change the bitmap the view displays, and 
 * setController() used to set the onFling behaviour.
 *  
 */
public class BitmapView extends View {
    private Bitmap mBitmap;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private IBitmapViewController mController;

    /*
     * Scale factor to get bitmap to fit on screen
     */
    private float mInitialScaleFactor = 1.0f;

    /*
     * Zoom factor user has requested
     */
    private float mZoomFactor = 1.0f;
    
    /*
     * The total available area for drawing on
     */
    private Rect mRawScreenDimensions;

    /*
     * Scale/Translate transform to draw wanted section of bitmap on screen
     */
    private Matrix mTransform = new Matrix();
    
    /*
     * Displacement (in bitmap co-ordinates) to pixel to draw in centre of screen
     */
    private PointF mScrollOffset = new PointF(0.0f, 0.0f);

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mScaleDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRawScreenDimensions = new Rect(0, 0, w, h);
        computeInitialMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            computeTransform();
            canvas.drawBitmap(mBitmap, mTransform, null);
        }
    }

    /*
     * Transform to draw desired part of bitmap on screen
     */
    private void computeTransform() {
        mTransform.reset();
        // move co-ords to centre of bitmap
        mTransform.postTranslate(-mBitmap.getWidth() / 2, -mBitmap.getHeight() / 2);

        // apply scroll offset
        mTransform.postTranslate(mScrollOffset.x, mScrollOffset.y);
        
        // scale to fit within screen
        mTransform.postScale(mInitialScaleFactor, mInitialScaleFactor);

        // apply zoom factor
        mTransform.postScale(mZoomFactor, mZoomFactor);
        
        // centre bitmap on screen
        mTransform.postTranslate(mRawScreenDimensions.width() / 2, mRawScreenDimensions.height() / 2);
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ZoomInOnPoint(e);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mController != null) {
                mController.onLongPress();
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scrollView(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean val = false;
            if (mController != null) {
                val = mController.onFling(e1, e2, velocityX, velocityY);
            }
            return val;
        }
    };

    private SimpleOnScaleGestureListener mScaleListener = new SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleView(detector.getScaleFactor());
            return true;
        }
    };

    /*
     * Zoom in the specified location of the screen
     */
    private void ZoomInOnPoint(MotionEvent e) {
        // zoom in
        float zoomFactor = 2.0f;
        scaleView(zoomFactor);

        // scroll so selected position is at centre of screen
        float deltaX = (e.getX() - (mRawScreenDimensions.width() / 2)) * zoomFactor;
        float deltaY = (e.getY() - (mRawScreenDimensions.height() / 2)) * zoomFactor;
        scrollView(deltaX, deltaY);
    }

    /*
     * @param controller the object that will handle fling and long press events
     */
    public void setController(IBitmapViewController controller) {
        mController = controller;
    }

    /*
     * @param bitmap the image to display
     */
    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mBitmap = bitmap;
            if (mRawScreenDimensions != null) {
                computeInitialMatrix();
                
                // draw new image
                invalidate();
            }
        }
    }

    private float calcAspectRatio(float width, float height) {
        return width / height;
    }

    /*
     * Initial matrix transforms to put whole bitmap on screen 
     */
    private void computeInitialMatrix()
    {
        if ((mBitmap != null) && (mRawScreenDimensions != null)) {
            float bitmapAspect = calcAspectRatio(mBitmap.getWidth(), mBitmap.getHeight());
            float screenAspect = calcAspectRatio(mRawScreenDimensions.width(), mRawScreenDimensions.height());
            float heightRatio = mRawScreenDimensions.height() / (float)mBitmap.getHeight();
            float widthRatio = mRawScreenDimensions.width() / (float)mBitmap.getWidth();
            if (bitmapAspect < screenAspect) {
                // screen is landscape (compared to bitmap) so height is
                // limiting factor
                mInitialScaleFactor = heightRatio;
            } else {
                // screen is portrait (compared to bitmap), so width is limiting
                // factor
                mInitialScaleFactor = widthRatio;
            }
            mZoomFactor = 1.0f;
            mScrollOffset = new PointF(0.0f, 0.0f);
        }
    }

    /*
     * Increase/Decrease the section taken from the bitmap to draw on the screen
     * 
     *  @param zoomFactor relative zoom factor.  (e.g. 2 = increase zoom in by 2) 
     */
    private void scaleView(float zoomFactor) {
        mZoomFactor *= zoomFactor;

        // maximum zoom is 16, minimum zoom is 1
        mZoomFactor = Math.max(1.0f, Math.min(mZoomFactor, 16.0f));
        clampView();
    }

    /*
     * Move the bitmap pixel that will be centred on screen
     * 
     * @param screenX number of screen pixels to move horizontally
     * @param screenY number of screen pixels to move vertically
     */
    private void scrollView(float screenX, float screenY) {
        float scale = mZoomFactor * mInitialScaleFactor * -1;
        float deltaX = screenX / scale;
        float deltaY = screenY / scale;
        mScrollOffset.offset(deltaX, deltaY);
        clampView();
    }

    /*
     * Prevent user from scrolling off the bitmap 
     */
    private void clampView() {
        // figure out number of bitmap pixels the screen shows
        float scaleFactor = mZoomFactor * mInitialScaleFactor;
        float screenWidth = mRawScreenDimensions.width() / scaleFactor;  
        float screenHeight = mRawScreenDimensions.height() / scaleFactor;
        
        // Compute horizontal scroll limit, to keep bitmap within screen
        //... If projected bitmap doesn't span whole screen width, don't allow 
        //... horizontal scroll, just centre it horizontally. (i.e. scollOffset = 0) 
        //... When doesn't span screen, bitmap pixels < screen pixels 
        float maxX = Math.max(0.0f, (mBitmap.getWidth() - screenWidth) / 2);
        
        // Same logic for vertical scroll
        float maxY = Math.max(0.0f, (mBitmap.getHeight() - screenHeight) / 2);
        
        float minX = -maxX;
        float minY = -maxY;
        
        // clamp the scroll
        if (maxX < mScrollOffset.x) {
            mScrollOffset.x = maxX;
        }
        if (mScrollOffset.x < minX) {
            mScrollOffset.x = minX;
        }
        if (maxY < mScrollOffset.y) {
            mScrollOffset.y = maxY;
        }
        if (mScrollOffset.y < minY) {
            mScrollOffset.y = minY;
        }
        
        // redraw bitmap on screen
        invalidate();
    }
}
