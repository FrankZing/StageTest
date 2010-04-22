package com.dell.stagecoach;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
// import android.os.Debug;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Stage extends ActivityGroup {

	private LinearLayout ll;
	private String TILE1 = "SampleTile";
	private String TILE2 = "SampleTile2";
	LocalActivityManager lam;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ll = (LinearLayout) findViewById(R.id.scroller);
		
		lam = this.getLocalActivityManager();
		
		Button b = (Button) findViewById(R.id.btn);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent i = new Intent();
				i.setClass(Stage.this, SampleTile.class);
				View v = lam.startActivity(TILE1, i).getDecorView();
				ll.addView(v);
			}
		});

		Button b1 = (Button) findViewById(R.id.btn1);
		b1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//refresh();
			}
		});
	}

	private void refresh() {
		// Get the images from the content provider
		//Debug.startMethodTracing("retrieveThumbnails");
		Cursor c = managedQuery(TileProvider.CONTENT_URI, null, null, null,
				null);
		try {
			if (ll != null && c.moveToFirst()) {
				do {
					c.getString(c.getColumnIndex(TileProvider.TILE_NAME));
					byte[] bitmapArray = c.getBlob(c
							.getColumnIndex(TileProvider.THUMBNAIL));
					Bitmap thumbnail = BitmapFactory.decodeByteArray(
							bitmapArray, 0, bitmapArray.length);

					ImageView iv = new ImageView(this);
					iv.setImageBitmap(thumbnail);
					ll.addView(iv);
				} while (c.moveToNext());
				ll.invalidate();
				//Debug.stopMethodTracing();
			}
		} finally {
			c.close();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//The intent info (the actions) will come from Package Manager
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Intent i = new Intent("com.dell.tile.ACTION_LAUNCH");
			
			View v = lam.startActivity(TILE2, i).getDecorView();
			ll.addView(v);
		}
		return super.onTouchEvent(event);
	}

}