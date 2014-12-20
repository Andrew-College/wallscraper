package com.andrew.wallscraper;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

import us.codecraft.xsoup.Xsoup;
import us.codecraft.xsoup.xevaluator.XElements;


public class SearchResultManager extends ListActivity implements SensorEventListener {

   SearchAdapter sAdapter;
   public wallDownloader wallDL;

   public int SFW = 100, SKETCHY = 10;
   public int GENERAL = 100, ANIME = 10, PEOPLE = 1;


   protected RelativeLayout footerView = null;
   protected LinearLayout generalArea = null;
   protected LinearLayout animeArea = null;
   protected LinearLayout peopleArea = null;
   protected LinearLayout sfwArea = null;
   protected LinearLayout sketchyArea = null;

   /**
    * Accelerometer
    */

   /**
    * 1st dimension is states(previous, difference, current)
    * 2nd dimension is values
    * e.g.
    * {
    * {1,2,3},
    * {1,0,0},
    * {0,2,3}
    * }
    */
   float[][] tiltAcceleration = new float[3][3];
   private SensorManager senSensorManager;
   private Sensor senAccelerometer;
   private float yPosition;
   private float cutoffPoint = 0.03f;
   private float strengthModifier = 1f;
   private boolean touched = false;

   private static final int REQUEST_CODE = 10;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);


      //create interaction adapter
      sAdapter = new SearchAdapter(getApplicationContext(), getResources(), this);


      // Put divider between search results and footerView
      getListView().setHeaderDividersEnabled(true);


      //Inflate footerView for footer_view.xml file
      footerView = (RelativeLayout) getLayoutInflater().inflate(R.layout.search_footer, null);

      //Add footerView to ListView
      getListView().addFooterView(footerView);


      /*
      * footer resources initialisation
      */
      //Initial implementation for "resolution" and "ratio" functionality, actual solution would have required custom
      // listviews that modify list views selection from radio select to checkbox select
//      Spinner resolutionSpinner = (Spinner) footerView.findViewById(R.id.Resolution);
//      ArrayAdapter<CharSequence> resolutionAdapter = ArrayAdapter.createFromResource(this,
//         R.array.RESOLUTIONS, android.R.layout.simple_spinner_item);
//
//      resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//      resolutionSpinner.setAdapter(resolutionAdapter);
//
//      Spinner ratioSpinner = (Spinner) footerView.findViewById(R.id.Ratio);
//
//      ArrayAdapter<CharSequence> ratioAdapter = ArrayAdapter.createFromResource(this,
//         R.array.RATIOS, android.R.layout.simple_spinner_item);
//
//      ratioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//      ratioSpinner.setAdapter(ratioAdapter);
      /*
      * footer settings initialisation
      */

//      sAdapter.add(new SearchResult("hello","hello","hello", new byte[0]));
      //retrieve properties
      generalArea = (LinearLayout) footerView.findViewById(R.id.General_area);
      animeArea = (LinearLayout) footerView.findViewById(R.id.Anime_area);
      peopleArea = (LinearLayout) footerView.findViewById(R.id.People_area);
      sfwArea = (LinearLayout) footerView.findViewById(R.id.sfw_area);
      sketchyArea = (LinearLayout) footerView.findViewById(R.id.sketchy_area);


      //////listener begin
      generalArea.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (((CheckBox) footerView.findViewById(R.id.general)).isChecked()) {
               ((CheckBox) footerView.findViewById(R.id.general)).setChecked(false);
               GENERAL = 0;
            } else {
               ((CheckBox) footerView.findViewById(R.id.general)).setChecked(true);
               GENERAL = Integer.parseInt(getResources().getString(R.string.GENERAL));
            }
//            System.out.println("general" + GENERAL);
         }
      });
      animeArea.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (((CheckBox) footerView.findViewById(R.id.anime)).isChecked()) {
               ((CheckBox) footerView.findViewById(R.id.anime)).setChecked(false);
               ANIME = 0;
            } else {
               ((CheckBox) footerView.findViewById(R.id.anime)).setChecked(true);
               ANIME = Integer.parseInt(getResources().getString(R.string.ANIME));
            }
//            System.out.println("anime " + ANIME);
         }
      });
      peopleArea.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (((CheckBox) footerView.findViewById(R.id.people)).isChecked()) {
               ((CheckBox) footerView.findViewById(R.id.people)).setChecked(false);
               PEOPLE = 0;
            } else {
               ((CheckBox) footerView.findViewById(R.id.people)).setChecked(true);
               PEOPLE = Integer.parseInt(getResources().getString(R.string.PEOPLE));
            }
//            System.out.println("People " + PEOPLE);
         }
      });
      sfwArea.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (((CheckBox) footerView.findViewById(R.id.sfw)).isChecked()) {
               ((CheckBox) footerView.findViewById(R.id.sfw)).setChecked(false);
               SFW = 0;
            } else {
               ((CheckBox) footerView.findViewById(R.id.sfw)).setChecked(true);
               SFW = Integer.parseInt(getResources().getString(R.string.SFW));
            }
//            System.out.println("SFW " + SFW);
         }
      });
      sketchyArea.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (((CheckBox) footerView.findViewById(R.id.sketchy)).isChecked()) {
               ((CheckBox) footerView.findViewById(R.id.sketchy)).setChecked(false);
               SKETCHY = 0;
            } else {
               ((CheckBox) footerView.findViewById(R.id.sketchy)).setChecked(true);
               SKETCHY = Integer.parseInt(getResources().getString(R.string.SKETCHY));
            }
//            System.out.println("sketchy " + SKETCHY);

         }
      });

      TextView searchCreate = (TextView) footerView.findViewById(R.id.NewSearch);

      final EditText searchQuery = (EditText) footerView.findViewById(R.id.SearchTextArea);
      searchCreate.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (footerView.findViewById(R.id.SearchText).getVisibility() == View.GONE) {
               footerView.findViewById(R.id.SearchText).setVisibility(
                  View.VISIBLE
               );
               Toast.makeText(getApplicationContext(), "Tap search again to run your query!", Toast.LENGTH_LONG).show();
            } else {
               InputMethodManager imm = (InputMethodManager) getSystemService(
                  Context.INPUT_METHOD_SERVICE);
               imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);

               footerView.findViewById(R.id.SearchText).setVisibility(
                  View.GONE
               );
               Toast.makeText(getApplicationContext(), "Searching for \"" + searchQuery.getText() + "\"...", Toast.LENGTH_LONG).show();
               if ((GENERAL + ANIME + PEOPLE) == 0 || (SFW + SKETCHY) == 0) {
                  Toast.makeText(getApplicationContext(), "Purity or categories value is 0!", Toast.LENGTH_SHORT).show();
                  resetCheckers();
               }
               wallDL = new wallDownloader();
               String queryURL =
                  "http://alpha.wallhaven.cc/search?" +
                     (searchQuery.getText().length() > 0 ? "q=" +
                        searchQuery.getText() + "&" : "") +
                     "categories=" + (GENERAL + ANIME + PEOPLE) +
                     "&purity=" + (SFW + SKETCHY) +
                     "&sorting=random&order=desc";
               System.out.println(queryURL);
               wallDL.setURL(queryURL);
               wallDL.execute();
            }

         }
      });

      TextView searchReset = (TextView) footerView.findViewById(R.id.ResetSearch);

      searchReset.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            ((TextView) footerView.findViewById(R.id.SearchTextArea)).setText("");
            //set checkers
            resetCheckers();
         }
      });

      TextView randomButton = (TextView) footerView.findViewById(R.id.RandomSearch);
      randomButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if ((GENERAL + ANIME + PEOPLE) == 0 || (SFW + SKETCHY) == 0) {
               Toast.makeText(getApplicationContext(), "Purity or categories value is 0!", Toast.LENGTH_SHORT).show();
               resetCheckers();
            }

            wallDL = new wallDownloader();
            String queryURL = "http://alpha.wallhaven.cc/search?categories=" + (GENERAL + ANIME + PEOPLE) + "&purity=" + (SFW + SKETCHY) + "&sorting=random&order=desc";
//            System.out.println(queryURL);
            wallDL.setURL(queryURL);
            wallDL.execute();
         }
      });

      /**
       * Accelerometer
       */
      //setup accelerometer
      senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

      final LinearLayout checkEnabledArea = (LinearLayout) footerView.findViewById(R.id.EnableDisableAccelerometer);
      final CheckBox checkEnabled = (CheckBox) footerView.findViewById(R.id.accelerometerChecker);

      checkEnabled.setOnTouchListener(new View.OnTouchListener() {

         @Override
         public boolean onTouch(View v, MotionEvent event) {

            touched = !touched;
            if (touched) {
//               Log.d("ENABLING/DISABLING", "...");
               checkEnabled.setChecked(!checkEnabled.isChecked());
               if (checkEnabled.isChecked()) {
//                  Log.d("ENABLED", "TRUE");
                  final long totalScrollTime = Long.MAX_VALUE; //total scroll time. I think that 300 000 000 years is close enough to infinity. if not enough you can restart timer in onFinish()

                  final int scrollPeriod = 20; // every 20 ms scoll will happened. smaller values for smoother

                  final int heightToScroll = 20; // will be scrolled to 20 px every time. smaller values for smoother scrolling

                  getListView().post(new Runnable() {
                     @Override
                     public void run() {
//                     Log.d("checker running","yup");
                        new CountDownTimer(totalScrollTime, scrollPeriod) {
                           public void onTick(long millisUntilFinished) {
                              //prevents running when the checkbox is unchecked
                              if (checkEnabled.isChecked()) {
                                 //prevent scrolling into lower oblivion
                                 if (getListView().getMaxScrollAmount() > (yPosition + tiltAcceleration[1][1])) {
                                    //prevent scrolling to upper oblivion
                                    if (getListView().getScrollY() + (yPosition + tiltAcceleration[1][1]) >= 0) {
                                       //prevents incrementing when device is stationery,
                                       //implying differences < "cutoffPoint" are just stationery values
                                       if (Math.abs(tiltAcceleration[1][1]) > cutoffPoint) {
                                          yPosition += tiltAcceleration[1][1];
                                          getListView().scrollBy(0, (int) yPosition);
//                                          Log.d("yposition", String.valueOf(yPosition));
                                       }
                                    } else {
                                       getListView().setScrollY(0);
                                       yPosition = 0;
                                    }
                                 } else {
                                    getListView().setScrollY(getListView().getMaxScrollAmount());
                                    yPosition = 0;
                                 }
                              }
                           }

                           public void onFinish() {
                              //you can add code for restarting timer here
                           }
                        }.start();
                     }
                  });
               }


            }
            return true;// touch event has been used, do not process event further
         }
      });

      Button configureButton = (Button) footerView.findViewById(R.id.accelerometerConfigure);
      configureButton.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
              Intent configureIntent = new Intent(SearchResultManager.this,AccelerometerConfig.class);

              configureIntent.setFlags(configureIntent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
              startActivityForResult(configureIntent,REQUEST_CODE);

              return true;
           }
        }
      );

//      getListView().setOnTouchListener(new View.OnTouchListener() {
//         @Override
//         public boolean onTouch(View v, MotionEvent event) {
//            Log.d("coordinate",String.valueOf(yPosition));
//            yPosition -= tiltAcceleration[1][1];
//            v.setScrollY((int) (v.getScrollY() + yPosition));
//            return true;
//         }
//      });
      //////listener end
      getListView().setAdapter(sAdapter);
   }


   public void resetCheckers() {

      Toast.makeText(getApplicationContext(), "resetting values", Toast.LENGTH_LONG).show();
      //set checkers
      ((CheckBox) footerView.findViewById(R.id.general)).setChecked(false);
      ((CheckBox) footerView.findViewById(R.id.anime)).setChecked(false);
      ((CheckBox) footerView.findViewById(R.id.people)).setChecked(false);
      ((CheckBox) footerView.findViewById(R.id.sfw)).setChecked(false);
      ((CheckBox) footerView.findViewById(R.id.sketchy)).setChecked(false);
      (generalArea).performClick();
      (animeArea).performClick();
      (peopleArea).performClick();
      (sfwArea).performClick();
      (sketchyArea).performClick();
   }

//   @Override
//   public boolean onTouchEvent(MotionEvent event){
//      event.offsetLocation(0, (float) (event.getY()+0.01));
//      return true;
//   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.search_result, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   /**
    * web page downloader; Stage 1  of wallpaper collection
    * <p/>
    * NOTE: CLASS MUST BE RE-INITIALIZED EACH TIME. Avoids some problems with multiple queries
    */
   class wallDownloader extends AsyncTask<Void, Void, String> {

      private static final String TAG = "HttpGetTask";

      private String URL = "";

      private String webText = "";

      private boolean retrieveImages = true;

      public String getURL() {
         return URL;
      }

      public void setURL(String URL) {
         this.URL = URL;
      }

      public String getWebText() {
         return webText;
      }

      public void setWebText(String webText) {
         this.webText = webText;
      }

      public boolean isRetrieveImages() {
         return retrieveImages;
      }

      public void setRetrieveImages(boolean retrieveImages) {
         this.retrieveImages = retrieveImages;
      }

      @Override
      protected String doInBackground(Void... params) {
         String data = null;

         HttpURLConnection httpUrlConnection = null;

         try {//download web page
            httpUrlConnection = (HttpURLConnection) new java.net.URL(URL)
               .openConnection();

            data = readStream(
               new BufferedInputStream(
                  httpUrlConnection.getInputStream()
               )
            );

         } catch (MalformedURLException exception) {
            Log.e(TAG, "MalformedURLException");
         } catch (IOException exception) {//user possibly disconnected from internet or  device is malfunctioning
            Log.e(TAG, "IOException: Downloading webpage " + this.URL + " failed");
         } finally {
            if (null != httpUrlConnection)
               httpUrlConnection.disconnect();
         }

//         save downloaded page temporarily
         return data;
      }

      @Override
      protected void onPostExecute(String result) {
//         try {
//            InputStream inputStream = new ByteArrayInputStream(Charset.forName("UTF-16").encode(
//               "<html><head></head><body><ul><li><hello></hello></li></ul></body></html>"
//            ).array());
//
//            InputSource iSource = new InputSource(inputStream);
//
//            XPathFactory xpf = XPathFactory.newInstance();
//
//            XPathExpression xpe = xpf.newXPath().compile("li/figure/hello");
//            String value = xpe.evaluate(iSource);
         if (result == null) {////notify user of of failure and return.
//            System.out.println("noResult");
            Toast.makeText(getApplicationContext(), "Could not retrieve wallpapers.", Toast.LENGTH_LONG).show();
            return;
         }

         if (isRetrieveImages()) {//either download images, or save the webpage
            ////Run jsoup html parsing engine, then run xsoup xslt query, example imagelist element that is tidied and is a successful xslt match;
            /**
             *   <li>
             *      <figure class="thumb purity-sfw " style="width:300px;height:200px">
             *         <img class="lazyload loaded" src="http://alpha.wallhaven.cc/wallpapers/thumb/small/th-60162.jpg" data-src="http://alpha.wallhaven.cc/wallpapers/thumb/small/th-60162.jpg" alt="loading">
             *         <a class="preview" target="_blank" href="http://alpha.wallhaven.cc/wallpaper/60162"></a>
             *         <div class="wall-info"> <== missing closing tags||
             *         <ul class="thumb-tags"> <=======================//
             *      </figure>
             *   </li>
             */
            XElements imageElements = Xsoup.select(result, "li/figure/img");
            XElements imageURLS = Xsoup.select(result, "section/ul/li/figure/a");

            List<String> imagelist = imageElements.list();
            List<String> URLlist = imageURLS.list();

//         System.out.println(" imagelist size " + imagelist.size());

            if (imagelist.size() <= 0) {//query had no valid results, notify & return
//            System.out.println("no stuff to get");
               Toast.makeText(getApplicationContext(), "No images found, try another search.", Toast.LENGTH_LONG).show();
               return;
            }

            for (int i = 0; i < imagelist.size(); i++) {
               ////Extract image path
               /**
                * start: <img class="lazyload loaded" src="http://alpha.wallhaven.cc/wallpapers/thumb/small/th-60162.jpg" data-src="http://alpha.wallhaven.cc/wallpapers/thumb/small/th-60162.jpg" alt="loading">
                */
               String imagePath = imagelist.get(i);

               /**
                * strip beginning: http://alpha.wallhaven.cc/wallpapers/thumb/small/th-60162.jpg" alt="loading">
                */
               imagePath = imagePath.substring(imagePath.indexOf("data-src=\"") + 10);

               /**
                * strip end: http://alpha.wallhaven.cc/wallpapers/thumb/small/th-60162.jpg
                */
               imagePath = imagePath.substring(0, imagePath.indexOf("\" src"));


               //extract image page URL, similar procedure as above, but on;
               //<a target="_blank" href="http://alpha.wallhaven.cc/wallpaper/25720" class="preview"></a>

               String imageURL = URLlist.get(i);
               imageURL = imageURL.substring(imageURL.indexOf("href=") + 6);
               imageURL = imageURL.substring(0, imageURL.indexOf("\""));

               //configure single image downloader
               contentDownloader cd = new contentDownloader();
               cd.setURL(imagePath);
               cd.setIMAGEPAGE(imageURL);
               cd.execute();
            }
         } else {//save downloaded text in the class
            setWebText(result);
         }
//         } catch (XPathExpressionException e) {
//            e.printStackTrace();
//         }

/*
         try {
            if (parseFactory == null) {
               parseFactory = XmlPullParserFactory.newInstance();
               parseFactory.setNamespaceAware(true);
               pullParser = parseFactory.newPullParser();
            }
            pullParser.setInput(new StringReader(result));
            if(pullParser.getLineNumber() == -1){
               pullParser.next();
            }
            int type = pullParser.getEventType();
            System.out.println(type);
            System.out.println("name" + pullParser.getAttributeName(pullParser.START_DOCUMENT) + "namespace; " + pullParser.getAttributeNamespace(pullParser.START_DOCUMENT));
            System.out.println("name" + pullParser.getAttributeName(pullParser.START_TAG) + "namespace; " + pullParser.getAttributeNamespace(pullParser.START_TAG));

            while (pullParser.getEventType() != pullParser.END_DOCUMENT) {

               System.out.println(pullParser.getNamespace());

               pullParser.next();
            }
         } catch (XmlPullParserException xe) {
            xe.printStackTrace();
            throw new RuntimeException();
         } catch (IOException e) {
            e.printStackTrace();
         }

*/
//         System.out.println(result); // write out webpage result
//         setListAdapter(new ArrayAdapter<String>(
//            SearchResultManager.this, R.layout.string_result, list));
      }

      private String readStream(InputStream in) {// convert stream buffer to string
         BufferedReader reader = null;
         StringBuffer data = new StringBuffer("");
         try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
               data.append(line);
            }
         } catch (IOException e) {
            Log.e(TAG, "IOException");
         } finally {
            if (reader != null) {
               try {
                  reader.close();
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }
         return data.toString();
      }


      /**
       * Image downloader; stage 2 of wallpaper collection
       * NOTE: CLASS MUST BE RE-INITIALIZED EACH TIME. Avoids some problems with multiple queries
       */
      class contentDownloader extends AsyncTask<Void, Void, String> {

         private static final String TAG = "ImageGetTask";

         private String URL = "", IMAGEPAGE = "";

         public void setURL(String URL) {
            this.URL = URL;
         }

         public void setIMAGEPAGE(String IMAGEPAGE) {
            this.IMAGEPAGE = IMAGEPAGE;
         }

         @Override
         protected String doInBackground(Void... params) {
            String data = null;

            HttpURLConnection httpUrlConnection = null;

            try {
               httpUrlConnection = (HttpURLConnection) new java.net.URL(URL)
                  .openConnection();

               data = Base64.encodeToString(//convert bytestream recieved to base64 encoded string through MigBase64
                  IOUtils.toByteArray(httpUrlConnection.getInputStream())//use apaches converter utils
                  , Base64.DEFAULT
               );

            } catch (MalformedURLException exception) {
               Log.e(TAG, "MalformedURLException");
            } catch (IOException exception) {// Error occurred downloading image,
               Log.e(TAG, "IOException: Downloading image; " + this.URL + " failed");
            } finally {
               if (null != httpUrlConnection)
                  httpUrlConnection.disconnect();
            }
            return data;
         }

         @Override
         protected void onPostExecute(String result) {
            if (result == null) {////notify user of of failure and return.
//            System.out.println("noResult");
               Toast.makeText(getApplicationContext(), "Could not retrieve wallpaper \"" + URL + "\".", Toast.LENGTH_LONG).show();
               return;
            }
            //decode base64 string back to byte array and add it to list
            sAdapter.add(new SearchResult(this.IMAGEPAGE, Base64.decode(result, Base64.DEFAULT)));

         }

      }

   }


   @Override
   protected void onPause() {
      super.onPause();
      senSensorManager.unregisterListener(this);
   }

   @Override
   protected void onResume() {
      super.onResume();
      senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
   }

   @Override
   public void onSensorChanged(SensorEvent event) {
      Sensor mySensor = event.sensor;

      if(Math.abs(event.values[0]) > cutoffPoint ||  Math.abs(event.values[1])> cutoffPoint) {
         if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();

//         if ((curTime - lastUpdate) > updateTimeout) {

            //previous
            tiltAcceleration[0][0] = tiltAcceleration[2][0];
            tiltAcceleration[0][1] = tiltAcceleration[2][1];
            tiltAcceleration[0][2] = tiltAcceleration[2][2];
            //current
            tiltAcceleration[2][0] = event.values[0];
            tiltAcceleration[2][1] = event.values[1];
            tiltAcceleration[2][2] = event.values[2];
            //difference
            tiltAcceleration[1][0] = strengthModifier * (tiltAcceleration[0][0] - tiltAcceleration[2][0]);
            tiltAcceleration[1][1] = strengthModifier * (tiltAcceleration[0][1] - tiltAcceleration[2][1]);
            tiltAcceleration[1][2] = strengthModifier * (tiltAcceleration[0][2] - tiltAcceleration[2][2]);

//           System.out.println("X" + tiltAcceleration[2][0] + "Y" + tiltAcceleration[2][1] + "Z" + tiltAcceleration[2][2]);

//         }
         }
      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data){
      super.onActivityResult(requestCode, resultCode, data);
      System.out.println("GOTTEN THIS FAR");
      if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
         if (data.hasExtra("returnkey")) {
            String result = data.getExtras().getString("returnkey");
            if (result != null && result.length() > 0) {
               Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            }
         }else if(data.hasExtra("newCutoff")){
            float result = data.getExtras().getFloat("newCutoff");
            cutoffPoint = result;
         }else if(data.hasExtra("strength")){
            float result = data.getExtras().getFloat("strength");
            strengthModifier = result;
         }
      }
   }
}

