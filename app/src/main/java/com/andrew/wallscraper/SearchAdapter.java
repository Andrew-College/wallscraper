package com.andrew.wallscraper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andmin on 21/11/2014.
 */
public class SearchAdapter extends BaseAdapter {

   private final List<SearchResult> results = new ArrayList<SearchResult>();
   private final Context context;
   private LayoutInflater inflater = null;
   private Resources parentResources = null;
   private Activity parent;

   public SearchAdapter(Context context, Resources parentResources, SearchResultManager parent) {
      this.context = context;
      this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      this.parentResources = parentResources;
      this.parent = parent;

   }

   public void add(SearchResult sResult){
      this.results.add(sResult);
      notifyDataSetChanged();
   }

   public void add(String resourceUrl, byte[] data){
//      JSONObject responseObject = new JSONObject();
//
//      try {
//         JSONObject ja = responseObject.getJSONObject("data");
//         JSONArray namesArray = responseObject.names();
//         JSONArray theData = responseObject.toJSONArray(namesArray);
//
//         theData.getJSONObject(0).
//      } catch (JSONException e) {
//         e.printStackTrace();
//      }

      this.results.add(

         new SearchResult(resourceUrl, data)

      );

      System.out.println("new data " + data.length);

      notifyDataSetChanged();
   }

   public void clear(){
      this.results.clear();
   }

   @Override
   public int getCount() {
      return results.size();
   }

   @Override
   public SearchResult getItem(int position) {
      return results.get(position);
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, final ViewGroup parent) {
      final SearchResult result = getItem(position);

      RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.activity_search_result, null);

      final Button fullImage = (Button) itemLayout.findViewById(R.id.FullImage);

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inMutable = true;
      Bitmap bitmap = BitmapFactory.decodeByteArray(result.getData(), 0, result.getData().length, options);
      Drawable drawable = new BitmapDrawable(this.parentResources, bitmap);
      itemLayout.setBackground(drawable);

      System.out.println(result.getResourceURL());

      fullImage.setHint(result.getResourceURL());

      fullImage.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullImage.getHint().toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
         }
      });

      return itemLayout;
   }
}
