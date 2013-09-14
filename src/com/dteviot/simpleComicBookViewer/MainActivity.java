package com.dteviot.simpleComicBookViewer;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    private BitmapView mBitmapView;
    private BitmapViewController mBitmapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBitmapView = (BitmapView) findViewById(R.id.comicView);
        mBitmapController = new BitmapViewController(mBitmapView, (Activity) this);
        mBitmapView.setController(mBitmapController);

        if (savedInstanceState != null) {
            // screen orientation changed, reload
            loadComic(new Bookmark(savedInstanceState));
        } else {
            // app has just been started.
            // If a bookmark has been saved, go to it, else, ask user for comic
            // to view
            Bookmark bookmark = new Bookmark(this);
            if (bookmark.isEmpty()) {
                launchComicList();
            } else {
                loadComic(bookmark);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_pick_comic:
            launchComicList();
            return true;
        case R.id.menu_bookmark:
            launchBookmarkDialog();
            return true;
        case R.id.menu_chapters:
            launchChaptersList();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Preserve settings. User is probably changing orientation
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mBitmapController.getBookmark().save(outState);
    }

    private void launchComicList() {
        Intent listComicsIntent = new Intent(this, ListComicsActivity.class);
        startActivityForResult(listComicsIntent, 0);
    }

    private void launchChaptersList() {
        IComic comic = mBitmapController.getComic();
        if (comic != null) {
            Intent listChaptersIntent = new Intent(this, ListChaptersActivity.class);
            listChaptersIntent.putExtra(ListComicsActivity.FILENAME_EXTRA, comic.getName());
            startActivityForResult(listChaptersIntent, 0);
        }
    }

    /*
     * Should return with comic to load
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String fileName = data.getStringExtra(ListComicsActivity.FILENAME_EXTRA);
            int page = data.getIntExtra(ListComicsActivity.PAGE_EXTRA, 0);
            loadComic(fileName, page);
        } else if (resultCode == RESULT_CANCELED) {
            Utility.showErrorToast(this, data);
        }
    }

    private void launchBookmarkDialog() {
        BookmarkDialog dlg = new BookmarkDialog(this);
        dlg.show(saveBookmark, loadBookmark);
    }

    private OnClickListener saveBookmark = new OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            mBitmapController.getBookmark().saveToSharedPreferences(MainActivity.this);
        }
    };

    private OnClickListener loadBookmark = new OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            loadComic(new Bookmark(MainActivity.this));
        }
    };

    private void loadComic(Bookmark bookmark) {
        if (!bookmark.isEmpty()) {
            loadComic(bookmark.getComicName(), bookmark.getPage());
        }
    }

    private void loadComic(String fileName, int page) {
        CbzComic comicBook = new CbzComic(fileName);
        mBitmapController.setComic(comicBook, page);
    }
}
