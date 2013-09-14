package com.dteviot.simpleComicBookViewer;

import java.util.ArrayList;

import android.graphics.Bitmap;

/*
 *  Abstract interface for a comic.
 *  Conceptually, a comic book is an array of images (bitmaps)
 *  Where each image is a page of the comic
 */
public interface IComic {
    /*
     * @return Identifier of the comic book
     */
    public String getName();

    /*
     * Clean up comic resources when we're done.
     * Usually, the comic book will be in a file, so
     * calling this will close the underlying file
     */
    public void close();

    /*
     * @return number of pages in comic
     */
    public int numPages();

    /*
     *  Retrieve a page's image, full size
     *  
     *  @param pageNum (zero based) index of page to fetch
     *  @return the page (or null if unable to fetch page)
     */
    public Bitmap getPage(int pageNum);

    /*
     * Retrieve a page's image, scaled down
     *
     *  @param pageNum (zero based) index of page to fetch
     *  @param maxLength Scale image so that height and width are no longer than this
     *  @return the page (or null if unable to fetch page)
     */
    public Bitmap getPageAsThumbnail(int pageNum, int maxLength);
    
    /*
     * The "chapters" of the comic
     * Often, the Zip file will contain multiple chapters.
     * Or multiple issues of comic in single Zip file.
     */
    public ArrayList<Chapter> getChapters();
}
