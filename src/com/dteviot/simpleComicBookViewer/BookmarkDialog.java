package com.dteviot.simpleComicBookViewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class BookmarkDialog {
    private Context mContext;

    public BookmarkDialog(Context context) {
        mContext = context;
    }

    public void show(OnClickListener setBookmark, OnClickListener restoreBookmark) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.bookmark_dialog_title);
        builder.setNegativeButton(R.string.bookmark_dialog_set_button, setBookmark);
        builder.setPositiveButton(R.string.bookmark_dialog_restore_button, restoreBookmark);
        builder.create().show();
    }
}
