package com.runapp.jdreddex.fitnessforrunners;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.runapp.jdreddex.fitnessforrunners.helpers.DbOpenHelper;
import com.runapp.jdreddex.fitnessforrunners.models.State;

/**
 * Created by JDReddex on 07.07.2016.
 */
public class App extends Application {
    private static App instance;
    private State state;
    private DbOpenHelper dbOpenHelper;
    private SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        createState();
        instance = this;
        dbOpenHelper = new DbOpenHelper(this);
        db = dbOpenHelper.getWritableDatabase();
        db.execSQL("VACUUM");
    }

    public State getState() {
        return state;
    }
    public void createState() {
        state = new State();
    }
    public static App getInstance() {
        return instance;
    }
    public SQLiteDatabase getDb() {
        return db;
    }
}
