package com.dteviot.simpleComicBookViewer;

/*
 * Details of a chapter in a comic book
 * Assumes a comic book may contain multiple chapters
 */
public class Chapter {
   /*
    *  Constructor
    * 
    *  @param name Name of the chapter 
    *  @param firstPageIndex Page of comic that is first page of chapter
    */
   public Chapter(String name, int firstPageIndex) {
       mName = name;
       mFirstPageIndex = firstPageIndex;
   }
   
   /*
    * @return Name of the chapter
    */
   public String getName() {
       return mName;
   }
   
   /*
    * @return Page of comic that is first page of chapter
    */
   public int getFirstPageIndex() {
       return mFirstPageIndex;
   }
   
   /*
    * Name of the chapter
    */
   private String mName;
   
   /*
    * Page of comic that is first page of chapter
    */
   private int mFirstPageIndex;
}
