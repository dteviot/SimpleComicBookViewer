package com.dteviot.simpleComicBookViewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

/*
 * Records a position in a comic the reader wishes to return to
 */
public class Bookmark {
    public static final String PREFS_NAME = "ComicViewerPrefsFile";
    public static final String PREFS_COMIC_NAME = "ComicName";
    public static final String PREFS_PAGE = "Page";

    private String mComicName;
    private int mPage;

    /*
     * ctor
     */
    public Bookmark(String comicName, int page) {
        mComicName = comicName;
        mPage = page;
    }

    /*
     * Called when user changed screen orientation
     */
    public Bookmark(Bundle savedInstanceState) {
        mComicName = savedInstanceState.getString(PREFS_COMIC_NAME);
        mPage = savedInstanceState.getInt(PREFS_PAGE);
    }

    /*
     * Retrieve from the Shared Preferences
     */
    public Bookmark(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mComicName = settings.getString(PREFS_COMIC_NAME, "");
        mPage = settings.getInt(PREFS_PAGE, -1);
    }

    /*
     * return true if bookmark is "empty", i.e. doesn't hold a useful value
     */
    public boolean isEmpty() {
        return ((mComicName == null) || (mComicName.length() <= 0) || (mPage < -1));
    }

    /*
     * Write the bookmark into a bundle (normally used when screen orientation
     * changing)
     */
    public void save(Bundle outState) {
        if (!isEmpty()) {
            outState.putString(PREFS_COMIC_NAME, mComicName);
            outState.putInt(PREFS_PAGE, mPage);
        }
    }

    /*
     * Write to persistent storage
     */
    public void saveToSharedPreferences(Context context) {
        if (!isEmpty()) {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREFS_COMIC_NAME, mComicName);
            editor.putInt(PREFS_PAGE, mPage);
            editor.commit();
        }
    }

    /*
     * The comic that has been bookmarked
     */
    public String getComicName() {
        return mComicName;
    }

    /*
     * Page of comic
     */
    public int getPage() {
        return mPage;
    }
}
