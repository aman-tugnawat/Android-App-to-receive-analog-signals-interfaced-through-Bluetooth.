package com.at.signalcloud;
/**
 * Created by Aman Tugnawat on 13-06-2013.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple Show database access helper class. 
 * Defines the basic CRUD operations (Create, Read, Update, Delete)
 * for the example, and gives the ability to list all users as well as
 * retrieve or modify a specific user. 
 */

public class SqlAdapter 
{
	//
	// Databsae Related Constants
	//
	private static final String DATABASE_NAME = "ECG";
    private static final String DATABASE_TABLE = "users";
    private static final int DATABASE_VERSION = 5;
    
	public static final String KEY_TITLE = "name";
	public static final String KEY_SEX = "sex";
	public static final String KEY_AGE = "age";
    public static final String KEY_BODY = "body";
    //public static final String KEY_DATE_TIME = "ecg_date_time"; 
    public static final String KEY_ROWID = "_id";
    
    
    private static final String TAG = "SQL_Db_Adapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation SQL statement
     */
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " ("
            		+ KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_TITLE + " text not null, " 
                    + KEY_SEX + " text not null, "
                    + KEY_AGE + " text not null, "
                    + KEY_BODY + " text not null, " ; 

    

    private final Context mCtx;

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
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public SqlAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public SqlAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new USER using the title, body and user date time provided. 
     * If the user is  successfully created return the new rowId
     * for that USER, otherwise return a -1 to indicate failure.
     * 
     * @param title the title of the 
     * @param body the body of the USER
     * @param userDateTime the date and time the user should remind the user
     * @return rowId or -1 if failed
     */
    public long createUser(String title, String sex, String age, String body, String userDateTime ) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_SEX, sex);
        initialValues.put(KEY_AGE, age);
        initialValues.put(KEY_BODY, body);
        //initialValues.put(KEY_DATE_TIME, userDateTime); 

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the user with the given rowId
     * 
     * @param rowId id of user to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteUser(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all users in the database
     * 
     * @return Cursor over all users
     */
    public Cursor fetchAllUsers() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_SEX, KEY_AGE, 
                KEY_BODY}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the user that matches the given rowId
     * 
     * @param rowId id of user to retrieve
     * @return Cursor positioned to matching user, if found
     * @throws SQLException if user could not be found/retrieved
     */
    public Cursor fetchUser(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_SEX, KEY_AGE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the user using the details provided. The user to be updated is
     * specified using the rowId, and it is altered to use the title, body and user date time
     * values passed in
     * 
     * @param rowId id of user to update
     * @param title value to set user title to
     * @param body value to set user body to
     * @param userDateTime value to set the user time. 
     * @return true if the user was successfully updated, false otherwise
     */
    public boolean updateUser(long rowId, String title, String sex, String age, String body, String userDateTime) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_SEX, sex);
        args.put(KEY_AGE, age);
        args.put(KEY_BODY, body);
        //args.put(KEY_DATE_TIME, userDateTime);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}




