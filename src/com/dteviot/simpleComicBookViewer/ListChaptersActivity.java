package com.dteviot.simpleComicBookViewer;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class ListChaptersActivity extends ListActivity {
    private ListView mListView;
    private ChapterListAdapter mChapterListAdapter;
    private ArrayList<Chapter> mChapters;
    private IComic mComic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = getListView(); // get the built-in ListView
        listChapters(getIntent());
        mChapterListAdapter = new ChapterListAdapter(this, mChapters);
        mListView.setAdapter(mChapterListAdapter);
    }

    /*
     * populate mChapters with chapters in comic
     */
    private void listChapters(Intent intent) {
        if (intent != null) {
            String fileName = intent.getStringExtra(ListComicsActivity.FILENAME_EXTRA);
            mComic = new CbzComic(fileName);
            mChapters = mComic.getChapters();
            if (mChapters.size() <= 0) {
                mComic.close();
                Utility.finishWithError(this, R.string.no_chapters_found);
            }
        }
    }

    /*
     * Class implementing the "ViewHolder pattern", for better ListView
     * performance
     */
    private static class ViewHolder {
        public ImageView imageView; // refers to ListView item's ImageView
        public Button readButton; // refers to ListView item's Play Button
    }

    /*
     * Populates entries on the list
     */
    private class ChapterListAdapter extends ArrayAdapter<Chapter> {
        private List<Chapter> mChapters;
        private LayoutInflater mInflater;

        public ChapterListAdapter(Context context, List<Chapter> chapters) {
            super(context, -1, chapters);
            this.mChapters = chapters;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder; // holds references to current item's GUI

            // if convertView is null, inflate GUI and create ViewHolder;
            // otherwise, get existing ViewHolder
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.comic_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView
                        .findViewById(R.id.comicTitleImageView);
                viewHolder.readButton = (Button) convertView.findViewById(R.id.readButton);
                convertView.setTag(viewHolder); // store as View's tag
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Populate the list item (view) with the comic's details
            Chapter chapter = mChapters.get(position);
            viewHolder.readButton.setText(chapter.getName());
            viewHolder.readButton.setTag(chapter);
            viewHolder.readButton.setOnClickListener(readButtonListener);

            // get thumb nail image
            new LoadThumbnailsTask().execute(viewHolder.imageView, chapter.getFirstPageIndex());

            return convertView;
        }

    }

    /*
     * respond to user clicking a "chapter" button by sending selected chapter start page
     * back to caller
     */
    OnClickListener readButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Chapter chapter = (Chapter) v.getTag();
            Intent intent = new Intent();
            intent.putExtra(ListComicsActivity.FILENAME_EXTRA, mComic.getName());
            intent.putExtra(ListComicsActivity.PAGE_EXTRA, chapter.getFirstPageIndex());
            setResult(RESULT_OK, intent);
            mComic.close();
            finish();
        }
    };

    private class LoadThumbnailsTask extends AsyncTask<Object, Object, Bitmap> {
        ImageView imageView;

        @Override
        protected Bitmap doInBackground(Object... params) {
            imageView = (ImageView) params[0];
            int page = (Integer)params[1];
            Bitmap bitmap = mComic.getPageAsThumbnail(page, imageView.getHeight());
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }
    }

}
