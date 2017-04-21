package com.example.sreer.sdsuchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreer on 03-04-2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "SDSUChat";
    private  Context mContext = null;

    // Table Names
    public static final String TABLE_USERS = "User";
    public static final String FILTER_TABLE_USERS = "FilterMetaData";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_COUNTRY ="country";
    private static final String KEY_STATE = "state";
    private static final String KEY_CITY = "city";
    private static final String KEY_YEAR= "year";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ( "
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_NICKNAME + " TEXT,"
                    + KEY_COUNTRY + " TEXT,"
                    + KEY_STATE + " TEXT,"
                    + KEY_CITY + " TEXT,"
                    + KEY_YEAR + " INTEGER,"
                    + KEY_LATITUDE + " REAL,"
                    + KEY_LONGITUDE + " REAL,"
                    + KEY_TIMESTAMP + " TEXT" +")";

    private static final String CREATE_FILTER_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS " + FILTER_TABLE_USERS + " ( "
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_NICKNAME + " TEXT,"
                    + KEY_COUNTRY + " TEXT,"
                    + KEY_STATE + " TEXT,"
                    + KEY_CITY + " TEXT,"
                    + KEY_YEAR + " INTEGER,"
                    + KEY_LATITUDE + " REAL,"
                    + KEY_LONGITUDE + " REAL,"
                    + KEY_TIMESTAMP + " TEXT" +")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        Log.i("database test","db helper constructor called");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        Log.i("database test","Table Created");
        Toast.makeText(mContext,"create executed",Toast.LENGTH_LONG);
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_FILTER_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        Toast.makeText(mContext,"onUpgrade",Toast.LENGTH_LONG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // create new tables
        onCreate(db);
    }

    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId());
        values.put(KEY_NICKNAME, user.getNickname());
        values.put(KEY_TIMESTAMP, user.getTimeStamp());
        values.put(KEY_COUNTRY, user.getCountry());
        values.put(KEY_STATE, user.getState());
        values.put(KEY_CITY, user.getCity());
        values.put(KEY_YEAR, user.getYear());
        values.put(KEY_LATITUDE, user.getLatitude());
        values.put(KEY_LONGITUDE, user.getLongitude());

        // insert row
        long user_row_id = db.replace(TABLE_USERS, null, values);

        return user_row_id;
    }

    public long addUserToFilter(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId());
        values.put(KEY_NICKNAME, user.getNickname());
        values.put(KEY_TIMESTAMP, user.getTimeStamp());
        values.put(KEY_COUNTRY, user.getCountry());
        values.put(KEY_STATE, user.getState());
        values.put(KEY_CITY, user.getCity());
        values.put(KEY_YEAR, user.getYear());
        values.put(KEY_LATITUDE, user.getLatitude());
        values.put(KEY_LONGITUDE, user.getLongitude());

        // insert row
        long user_row_id = db.replace(FILTER_TABLE_USERS, null, values);

        return user_row_id;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + TABLE_USERS+" ORDER BY "+KEY_ID+" DESC LIMIT 25";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                User user = new User();
                user.setYear(c.getInt((c.getColumnIndex(KEY_YEAR))));
                user.setNickname((c.getString(c.getColumnIndex(KEY_NICKNAME))));
                user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                user.setTimeStamp(c.getString(c.getColumnIndex(KEY_TIMESTAMP)));
                user.setCountry(c.getString(c.getColumnIndex(KEY_COUNTRY)));
                user.setState(c.getString(c.getColumnIndex(KEY_STATE)));
                user.setCity(c.getString(c.getColumnIndex(KEY_CITY)));
                user.setLatitude(c.getFloat(c.getColumnIndex(KEY_LATITUDE)));
                user.setLongitude(c.getFloat(c.getColumnIndex(KEY_LONGITUDE)));

                // adding to user list
                userList.add(user);
            } while (c.moveToNext());
        }

        return userList;
    }

    public List<User> getUsers(int id) {
        List<User> userList = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + TABLE_USERS+" WHERE "+KEY_ID+"<"+id+" ORDER BY "+KEY_ID+" DESC LIMIT 25";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                User user = new User();
                user.setYear(c.getInt((c.getColumnIndex(KEY_YEAR))));
                user.setNickname((c.getString(c.getColumnIndex(KEY_NICKNAME))));
                user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                user.setTimeStamp(c.getString(c.getColumnIndex(KEY_TIMESTAMP)));
                user.setCountry(c.getString(c.getColumnIndex(KEY_COUNTRY)));
                user.setState(c.getString(c.getColumnIndex(KEY_STATE)));
                user.setCity(c.getString(c.getColumnIndex(KEY_CITY)));
                user.setLatitude(c.getFloat(c.getColumnIndex(KEY_LATITUDE)));
                user.setLongitude(c.getFloat(c.getColumnIndex(KEY_LONGITUDE)));

                // adding to user list
                userList.add(user);
            } while (c.moveToNext());
        }

        return userList;
    }

    public boolean isDataAvailable(int id) {
        List<User> userList = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + TABLE_USERS+" WHERE "+KEY_ID+"<"+id+" ORDER BY "+KEY_ID+" DESC LIMIT 25";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.getCount()>0) {
            return true;
        }

        return false;
    }

    public List<User> getAllUsersFromFilter() {
        List<User> userList = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + FILTER_TABLE_USERS+" ORDER BY "+KEY_ID+" DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                User user = new User();
                user.setYear(c.getInt((c.getColumnIndex(KEY_YEAR))));
                user.setNickname((c.getString(c.getColumnIndex(KEY_NICKNAME))));
                user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                user.setTimeStamp(c.getString(c.getColumnIndex(KEY_TIMESTAMP)));
                user.setCountry(c.getString(c.getColumnIndex(KEY_COUNTRY)));
                user.setState(c.getString(c.getColumnIndex(KEY_STATE)));
                user.setCity(c.getString(c.getColumnIndex(KEY_CITY)));
                user.setLatitude(c.getFloat(c.getColumnIndex(KEY_LATITUDE)));
                user.setLongitude(c.getFloat(c.getColumnIndex(KEY_LONGITUDE)));

                // adding to user list
                userList.add(user);
            } while (c.moveToNext());
        }

        return userList;
    }

    public int getTotalCount() {

        String selectQuery = "SELECT  * FROM " + TABLE_USERS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        return c.getCount();
    }

    public int getMaxId(){

        String selectQuery = "SELECT max("+KEY_ID+") as id FROM "+TABLE_USERS;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        int maxid = cursor.getInt(cursor.getColumnIndex(KEY_ID));

        return maxid;

    }

    public int getFilterDataCount(String filter){

        String selectQuery = "SELECT * FROM "+FILTER_TABLE_USERS+" WHERE "+filter+" ORDER BY "+KEY_ID+" DESC";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery,null);

        return cursor.getCount();

    }

    public boolean isDataAlreadyAvailable(User user){
        String selectQuery = "SELECT * FROM "+FILTER_TABLE_USERS+" WHERE "+KEY_ID+"="+user.getId();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery,null);
        if(cursor.getCount()==0)
            return false;
        return true;
    }


    public List<User> getUsersFromFilter(String filter) {
        List<User> userList = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + FILTER_TABLE_USERS + " WHERE "+filter+ " ORDER BY "+KEY_ID+" DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                User user = new User();
                user.setYear(c.getInt((c.getColumnIndex(KEY_YEAR))));
                user.setNickname((c.getString(c.getColumnIndex(KEY_NICKNAME))));
                user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                user.setTimeStamp(c.getString(c.getColumnIndex(KEY_TIMESTAMP)));
                user.setCountry(c.getString(c.getColumnIndex(KEY_COUNTRY)));
                user.setState(c.getString(c.getColumnIndex(KEY_STATE)));
                user.setCity(c.getString(c.getColumnIndex(KEY_CITY)));
                user.setLatitude(c.getFloat(c.getColumnIndex(KEY_LATITUDE)));
                user.setLongitude(c.getFloat(c.getColumnIndex(KEY_LONGITUDE)));

                // adding to user list
                userList.add(user);
            } while (c.moveToNext());
        }
        return userList;
    }

    public List<User> getUsersFromFilter(String filter,int id) {
        List<User> userList = new ArrayList<User>();

        String selectQuery = "SELECT  * FROM " + TABLE_USERS+" WHERE "+KEY_ID+"<"+id+" AND "+filter+" ORDER BY "+KEY_ID+" DESC LIMIT 25";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                User user = new User();
                user.setYear(c.getInt((c.getColumnIndex(KEY_YEAR))));
                user.setNickname((c.getString(c.getColumnIndex(KEY_NICKNAME))));
                user.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                user.setTimeStamp(c.getString(c.getColumnIndex(KEY_TIMESTAMP)));
                user.setCountry(c.getString(c.getColumnIndex(KEY_COUNTRY)));
                user.setState(c.getString(c.getColumnIndex(KEY_STATE)));
                user.setCity(c.getString(c.getColumnIndex(KEY_CITY)));
                user.setLatitude(c.getFloat(c.getColumnIndex(KEY_LATITUDE)));
                user.setLongitude(c.getFloat(c.getColumnIndex(KEY_LONGITUDE)));

                // adding to user list
                userList.add(user);
            } while (c.moveToNext());
        }
        return userList;
    }

    public boolean isMoreFilterDataAvailable(String filter,int id) {
        List<User> userList = new ArrayList<User>();

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " WHERE " + KEY_ID + "<" + id + " AND " + filter + " ORDER BY " + KEY_ID + " DESC LIMIT 25";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.getCount() > 0) {
            return true;

        }
        return false;
    }

}

