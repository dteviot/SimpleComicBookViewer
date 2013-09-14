package com.dteviot.simpleComicBookViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class ListComicsActivity extends ListActivity {
    public static final String FILENAME_EXTRA = "FILENAME_EXTRA";
    public static final String PAGE_EXTRA = "PAGE_EXTRA";

    private ListView mListView;
    private ComicListAdapter mComicListAdapter;
    private String mRootPath;
    private ArrayList<String> mFileNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = getListView(); // get the built-in ListView
        listComicFiles();
        mComicListAdapter = new ComicListAdapter(this, mFileNames);
        mListView.setAdapter(mComicListAdapter);
    }

    /*
     * returns true if SD card storage (or equivalent) is available
     */
    private boolean isMediaAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }
    }

    /*
     * populate mFileNames with all files in Downloads directory of SD card
     */
    private void listComicFiles() {
        if (!isMediaAvailable()) {
            Utility.finishWithError(this, R.string.sd_card_not_mounted);
        } else {
            File path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            mRootPath = path.toString();
            mFileNames = new ArrayList<String>();
            String[] filesInDirectory = path.list();
            if (filesInDirectory != null) {
                for (String fileName : filesInDirectory) {
                    if (isComicBookFile(fileName)) {
                        mFileNames.add(fileName);
                    }
                }
            }
            if (mFileNames.isEmpty()) {
                Utility.finishWithError(this, R.string.no_comics_found);
            }
            Collections.sort(mFileNames, String.CASE_INSENSITIVE_ORDER);
        }
    }

    /*
     * returns true if filename ends in zip or cbz
     */
    private boolean isComicBookFile(String fileName) {
        Pattern pattern = Pattern.compile(".*zip$|.*cbz$");
        return pattern.matcher(fileName.toLowerCase()).matches();
    }
    
    private String titleToFileName(String title) {
        return mRootPath + "/" + title;
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
    private class ComicListAdapter extends ArrayAdapter<String> {
        private List<String> mTitles;
        private LayoutInflater mInflater;

        public ComicListAdapter(Context context, List<String> titles) {
            super(context, -1, titles);
            this.mTitles = titles;
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
            String title = mTitles.get(position);
            viewHolder.readButton.setText(title);
            viewHolder.readButton.setTag(title);
            viewHolder.readButton.setOnClickListener(readButtonListener);

            // get thumb nail image
            new LoadThumbnailsTask().execute(viewHolder.imageView, titleToFileName(title));

            return convertView;
        }

    }

    /*
     * respond to user clicking a "Read" button by sending selected filename
     * back to caller
     */
    OnClickListener readButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String fileName = titleToFileName((String) v.getTag());
            Intent intent = new Intent();
            intent.putExtra(FILENAME_EXTRA, fileName);
            // set page to first, because ListChaptersActivity returns page to start at
            intent.putExtra(PAGE_EXTRA, 0);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private class LoadThumbnailsTask extends AsyncTask<Object, Object, Bitmap> {
        ImageView imageView;

        @Override
        protected Bitmap doInBackground(Object... params) {
            imageView = (ImageView) params[0];
            CbzComic comic = new CbzComic((String) params[1]);
            Bitmap bitmap = comic.getPageAsThumbnail(0, imageView.getHeight());
            comic.close();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }
    }

}
