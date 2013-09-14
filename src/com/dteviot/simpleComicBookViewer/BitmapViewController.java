package com.dteviot.simpleComicBookViewer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.MotionEvent;

public class BitmapViewController implements IBitmapViewController {
    private BitmapView mBitmapView;
    private IComic mComic;
    private int mCurrentPage;
    private Activity mActivity;

    public BitmapViewController(BitmapView bitmapView, Activity activity) {
        mBitmapView = bitmapView;
        mActivity = activity;
    }

    public void setComic(IComic comic, int currentPage) {
        if (mComic != null) {
            mComic.close();
        }
        mComic = comic;
        mCurrentPage = currentPage;
        showPage();
    }

    /*
     * Return bookmark indicating the current comic and page being viewed
     */
    public Bookmark getBookmark() {
        return new Bookmark(mComic == null ? null : mComic.getName(), mCurrentPage);
    }

    public int getPage() {
        return mCurrentPage;
    }

    /*
     * @return the comic being viewed
     */
    public IComic getComic() {
        return mComic;
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // because UI needs to respond to both scroll and fling events
        // we do some thresholding, to ignore small flings that may have been 
        // unintentionally generated as part of a scroll.
        // The thresholds are somewhat arbitrary, and may require tuning.
        // TODO: allow user to customise thresholds via settings?
        int minDistance = mBitmapView.getWidth() / 2;
        float minVelocity = minDistance * 3;  
        float distanceX = Math.abs(e2.getX() - e1.getX());
        boolean processed = false;
        if ((minDistance < distanceX) && (minVelocity < Math.abs(velocityX))) {
            if (0 < velocityX) {
                backPage();
            } else {
                forwardPage();
            }
            processed = true;
        }
        return processed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dteviot.simpleComicBookViewer.IBitmapViewController#onLongPress()
     */
    @Override
    public void onLongPress() {
        toggleActionBarVisibility();
    }

    /*
     * Hide/Show action bar.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void toggleActionBarVisibility() {
        if (android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
	        ActionBar actionBar = mActivity.getActionBar();
	        if (actionBar.isShowing()) {
	            actionBar.hide();
	        } else {
	            actionBar.show();
	        }
        }
    }

    private void forwardPage() {
        if (mCurrentPage < mComic.numPages() - 1) {
            ++mCurrentPage;
            showPage();
        } else {
            Utility.showToast(mActivity, R.string.last_page);
        }
    }

    private void backPage() {
        if (0 < mCurrentPage) {
            --mCurrentPage;
            showPage();
        } else {
            Utility.showToast(mActivity, R.string.last_page);
        }
    }

    private void showPage() {
        mCurrentPage = Math.max(0, mCurrentPage);
        mCurrentPage = Math.min(mCurrentPage, mComic.numPages() - 1);

        // handle case of comic having no pages
        Bitmap bitmap = (0 <= mCurrentPage) ? mComic.getPage(mCurrentPage) : null;
        if (bitmap != null) {
            mBitmapView.setBitmap(bitmap);
        }
    }
}
