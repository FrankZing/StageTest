package com.dell.stagecoach;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;

public class SampleTile extends Activity {
	View child;
	public static String tname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tname = this.getClass().getName();
		setContentView(R.layout.child);
		child = findViewById(R.id.child);
	}
	
	@Override
	protected void onPause() {
		Bitmap outBitmap = Bitmap.createBitmap(child.getWidth(), child.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(outBitmap);
		//onCreateThumbnail(outBitmap, canvas);
		super.onPause();
	}
	
	@Override
	public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
		//Debug.startMethodTracing("onCreateThumbnail");
		boolean b = super.onCreateThumbnail(outBitmap, canvas);
		ContentValues values = new ContentValues();
		values.put(TileProvider.TILE_NAME, tname);
		//Create the byte array from the bitmap
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		outBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
		byte[] bitmapArray = baos.toByteArray();
		values.put(TileProvider.THUMBNAIL, bitmapArray);
		getContentResolver().insert(TileProvider.CONTENT_URI, values);
		//Debug.stopMethodTracing();
		return b;
	}
}
