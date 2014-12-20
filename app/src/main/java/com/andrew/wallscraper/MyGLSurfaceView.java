package com.andrew.wallscraper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

class MyGLSurfaceView extends GLSurfaceView {
   com.andrew.wallscraper.Renderer renderer;

   public MyGLSurfaceView(Context context) {
      super(context);
      setEGLContextClientVersion(2);
      renderer = new com.andrew.wallscraper.Renderer(context);
      setRenderer(renderer);
   }

   public MyGLSurfaceView(Context context, AttributeSet attrs) {
      super(context, attrs);
      setEGLContextClientVersion(2);
      renderer = new com.andrew.wallscraper.Renderer(context);
      setRenderer(renderer);
   }

   public void passArray(float[] values){
      renderer.setCoords(values);
   }
}