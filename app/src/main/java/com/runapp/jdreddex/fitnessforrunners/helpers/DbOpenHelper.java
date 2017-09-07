package com.runapp.jdreddex.fitnessforrunners.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {
    private static final String APP_DB_NAME = "app-db.db";
    private static final int DB_VERSION = 2;
    private final Context mContext;

    public DbOpenHelper(Context context) {
        super(context, APP_DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 1; i <= DB_VERSION; i++) {
            migrate(db, i);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        while (oldVersion < newVersion) {
            oldVersion += 1;
            migrate(db, oldVersion);
        }
    }

    protected void migrate(SQLiteDatabase db, int dbVersion) {
        switch (dbVersion) {
            case 1: {
                db.execSQL("CREATE TABLE reminder (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, time TEXT NOT NULL)");
                db.execSQL("CREATE TABLE track (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, startTime INTEGER NOT NULL, time INTEGER NOT NULL DEFAULT 0, distance INTEGER NOT NULL DEFAULT 0, synchronized INTEGER NOT NULL DEFAULT 0)");
                db.execSQL("CREATE TABLE track_gps (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, trackId INTEGER NOT NULL, lat REAL NOT NULL, lng REAL NOT NULL)");

                db.execSQL("CREATE INDEX track_startTime ON track (startTime)");
                db.execSQL("CREATE INDEX track_time ON track (time)");
                db.execSQL("CREATE INDEX track_distance ON track (distance)");
                db.execSQL("CREATE INDEX track_synchronized ON track (synchronized)");

                db.execSQL("CREATE INDEX reminder_time ON reminder (time)");

                db.execSQL("CREATE INDEX track_gps_trackId ON track_gps (trackId)");
                db.execSQL("CREATE INDEX track_gps_lat ON track_gps (lat)");
                db.execSQL("CREATE INDEX track_gps_lng ON track_gps (lng)");
                break;
            }
            case 2:{
                db.execSQL("ALTER TABLE track ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0");
                db.execSQL("UPDATE track SET favorite = 0");
                db.execSQL("CREATE INDEX track_favorite ON track (favorite)");
            }
        }
    }
}
