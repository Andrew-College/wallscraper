package com.andrew.wallscraper;

/**
 * Created by Andmin on 21/11/2014.
 */
public class SearchResult {

   private final String resourceURL;
   private final byte[] data;

   public SearchResult() {

      this.resourceURL = "";
      this.data = new byte[0];
   }

   public SearchResult(String resourceURL, byte[] data) {

      this.resourceURL = resourceURL;
      this.data = data;
   }

   public String getResourceURL() {
      return resourceURL;
   }

   public byte[] getData() {
      return data;
   }
}
