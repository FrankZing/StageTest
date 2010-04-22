package com.dell.stagecoach;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class TileProvider extends ContentProvider {

	 public static final String PROVIDER_NAME = "com.dell.stagecoach.TileProvider";
     public static final Uri CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/thumbnails");

     //Column Names
     public static final String _ID = "_id";
     public static final String TILE_NAME = "packageName";
     public static final String THUMBNAIL = "thumbnail"; 

     private static final int THUMBS = 1;
     private static final int THUMBS_ID = 2;
     
     private static final UriMatcher uriMatcher;
      static{
         uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         uriMatcher.addURI(PROVIDER_NAME, "thumbnails", THUMBS);
         uriMatcher.addURI(PROVIDER_NAME, "thumbnails/#", THUMBS_ID);      
      }

     
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		int count=0;
		switch (uriMatcher.match(arg0)){
		case THUMBS:
			count = tilesDB.delete(
					DATABASE_TABLE,
					arg1, 
					arg2);
			break;
		case THUMBS_ID:
			String id = arg0.getPathSegments().get(1);
			count = tilesDB.delete(
					DATABASE_TABLE,                        
					_ID + " = " + id + 
					(!TextUtils.isEmpty(arg1) ? " AND (" + 
							arg1 + ')' : ""), 
							arg2);
			break;
		default: throw new IllegalArgumentException(
				"Unknown URI " + arg0);    
		}       
		getContext().getContentResolver().notifyChange(arg0, null);
		return count;      

	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
        //---get all thumbnails---
        case THUMBS:
           return "vnd.android.cursor.dir/com.dell.stagecoach.thumbnails ";
        //---get a particular book---
        case THUMBS_ID:                
           return "vnd.android.cursor.item/com.dell.stagecoach.thumbnails";
        default:
           throw new IllegalArgumentException("Unsupported URI: " + uri);        
		}   
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//---add a new tile---
		long rowID = tilesDB.insert(
				DATABASE_TABLE, "", values);

		//---if added successfully---
		if (rowID>0)
		{
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);    
			return _uri;                
		}        
		throw new SQLException("Failed to insert row into " + uri);
	}


	@Override
	public boolean onCreate() {
		try {
			Context context = getContext();
		    DatabaseHelper dbHelper = new DatabaseHelper(context);
		    tilesDB = dbHelper.getWritableDatabase();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	    return (tilesDB == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
		      String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(DATABASE_TABLE);
	       
		if (uriMatcher.match(uri) == THUMBS_ID)
			//---if getting a particular book---
			sqlBuilder.appendWhere(
					_ID + " = " + uri.getPathSegments().get(1));                

		if (sortOrder==null || sortOrder=="")
			sortOrder = TILE_NAME;

		Cursor c = sqlBuilder.query(
				tilesDB, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);

		//---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, 
		      String selection, String[] selectionArgs) {
		int count = 0;
	      switch (uriMatcher.match(uri)){
	         case THUMBS:
	            count = tilesDB.update(
	               DATABASE_TABLE, 
	               values,
	               selection, 
	               selectionArgs);
	            break;
	         case THUMBS_ID:                
	            count = tilesDB.update(
	               DATABASE_TABLE, 
	               values,
	               _ID + " = " + uri.getPathSegments().get(1) + 
	               (!TextUtils.isEmpty(selection) ? " AND (" + 
	                  selection + ')' : ""), 
	                selectionArgs);
	            break;
	         default: throw new IllegalArgumentException(
	            "Unknown URI " + uri);    
	      }       
	      getContext().getContentResolver().notifyChange(uri, null);
	      return count;

	}
	
	//---for database use---
	private SQLiteDatabase tilesDB;
	private static final String DATABASE_NAME = "Tiles";
	private static final String DATABASE_TABLE = "tileThumbnails";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE =
	         "create table " + DATABASE_TABLE + 
	         " (_id integer primary key autoincrement, "
	         + "packageName text not null, thumbnail blob);";

	private static class DatabaseHelper extends SQLiteOpenHelper 
	   {
	      DatabaseHelper(Context context) {
	         super(context, DATABASE_NAME, null, DATABASE_VERSION);
	      }

	      @Override
	      public void onCreate(SQLiteDatabase db) 
	      {
	         db.execSQL(DATABASE_CREATE);
	      }

	      @Override
	      public void onUpgrade(SQLiteDatabase db, int oldVersion, 
	    		  				int newVersion) {
	         Log.w("Content provider database", 
	              "Upgrading database from version " + 
	              oldVersion + " to " + newVersion + 
	              ", which will destroy all old data");
	         db.execSQL("DROP TABLE IF EXISTS packageName");
	         onCreate(db);
	      }

	   }   

}
