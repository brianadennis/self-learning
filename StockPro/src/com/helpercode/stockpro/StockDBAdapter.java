package com.helpercode.stockpro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This is an adapter class that defines the way information is stored and accessed into
 * the internal SQLight database.
 * 
 * @author Brian Dennis
 * @version 9-11-2014
 */
public class StockDBAdapter {

	public static final String SYMBOL = "stocksymbol";
	public static final String NAME = "name";
	public static final String CURPRICE = "curprice";
	public final static String PERCENTAGE = "percentage";
	public static final String HIGH = "high";
	public static final String LOW = "low";
	public static final String VOLUME = "volume";
	public static final String CHANGE = "change";
	public static final String DATE_AND_TIME = "dateandtime";
	public static final String OLD_PRICE = "oldprice";
	public final static String AMOUNT = "amount";
	final static String REMOVE = "x";
	static final String ROWID = "_id";
	
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE = "CREATE TABLE stocks " 
    										+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT," 
    										+ "stocksymbol TEXT,"
    										+ "name TEXT," 
    										+ "curprice TEXT," 
    										+ "volume TEXT," 
    										+ "percentage TEXT," 
    										+ "change TEXT," 
    										+ "high TEXT," 
    										+ "low TEXT," 
    										+ "amount INTEGER," 
    										+ "x TEXT,"
    										+"dateandtime TEXT," +
    										"oldprice TEXT);";
    
    private static final String DATABASE_NAME = "stockdata";
    private static final String DATABASE_TABLE = "stocks";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;
    private static final String TAG = "StockDBDataProvider";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(final Context context) {
        	
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
        														  final int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS stocks");
            onCreate(db);
        }
    }

    public StockDBAdapter(final Context ctx) {
        this.mCtx = ctx;
    }

    public StockDBAdapter open() throws SQLException {
    	Log.d("StockDBAdapter","database Opened");
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
    	Log.d("StockDBAdapter","database Closed");
        mDbHelper.close();
    }

    /**
     * used when a user inputs a new stock.
     */
    public long createStock(final String symbol,final String name,final String curprice,
    		final String volume,final String percentage,final String change,
    		final String high,final String low, final int amount,final String x,
    		final String dateandtime,final String oldprice) {
    	
        final ContentValues initialValues = new ContentValues();
        
        initialValues.put(SYMBOL, symbol);
        initialValues.put(CURPRICE, curprice);
        initialValues.put(PERCENTAGE, percentage);
        initialValues.put(NAME, name);
        initialValues.put(HIGH, high);
        initialValues.put(LOW, low);
        initialValues.put(VOLUME, volume);
        initialValues.put(CHANGE, change);
        initialValues.put(AMOUNT, amount);
        initialValues.put(REMOVE, x);
        initialValues.put(DATE_AND_TIME, dateandtime);
        initialValues.put(OLD_PRICE, oldprice);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);  
    }
    
    /**
     * Used when a user inputs an existing stock.
     */
    public boolean updateStock(final String symbol, final String name, 
    		final String curprice, final String percentage, final String volume, 
    		final String change, final String high, final String low, final int amount, 
    		final String dateandtime, final String oldprice) {
    	
        final ContentValues changedValues = new ContentValues();
      
        changedValues.put(CURPRICE, curprice);
        changedValues.put(PERCENTAGE, percentage);
        changedValues.put(NAME, name);
        changedValues.put(HIGH, high);
        changedValues.put(LOW, low);
        changedValues.put(VOLUME, volume);
        changedValues.put(CHANGE, change);
        changedValues.put(AMOUNT, amount);
        changedValues.put(DATE_AND_TIME, dateandtime);
        changedValues.put(OLD_PRICE, oldprice);
        
        return mDb.update(DATABASE_TABLE, changedValues, 
        										 SYMBOL + "='" + symbol + "'", null) > 0;
    }
    
    public boolean refreshStocks(final String symbol, final String name, 
    		final String curprice, final String percentage, final String volume, 
    		final String change, final String high, final String low, 
    		final String dateandtime, final String oldprice) {

		final ContentValues changedValues = new ContentValues();
		
		changedValues.put(CURPRICE, curprice);
		changedValues.put(PERCENTAGE, percentage);
		changedValues.put(NAME, name);
		changedValues.put(HIGH, high);
		changedValues.put(LOW, low);
		changedValues.put(VOLUME, volume);
		changedValues.put(CHANGE, change);
		changedValues.put(DATE_AND_TIME, dateandtime);
		changedValues.put(OLD_PRICE, oldprice);

		return mDb.update(DATABASE_TABLE, changedValues, 
								 				 SYMBOL + "='" + symbol + "'", null) > 0;
    }
 
    public boolean deleteStock(final String symbol) {
    	
        return mDb.delete(DATABASE_TABLE,SYMBOL + "='" + symbol + "'", null) > 0;
    }
    
    public Cursor fetchAllStocks() {
    	
    	final Cursor mCursor = mDb.query(DATABASE_TABLE, new String[] {
    			  			   ROWID, SYMBOL, NAME, CURPRICE, PERCENTAGE, VOLUME, CHANGE, 
    			  			   HIGH, LOW, AMOUNT, REMOVE, DATE_AND_TIME, OLD_PRICE}, 
        					  							   null, null, null, null, null);
        if (mCursor != null) {
        	
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor fetchStockBySymbol(final String symbol) throws SQLException {
    	
        final Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
        							   ROWID, NAME, CURPRICE, PERCENTAGE, VOLUME, CHANGE, 
        							   HIGH, LOW, AMOUNT, REMOVE, DATE_AND_TIME}, 
            				 SYMBOL + "='" + symbol + "'", null, null, null, null, null);
        if (mCursor != null) {
        	
            mCursor.moveToFirst();
        }
        return mCursor;
    }  
    
    /**
     * Just returns the current amount of the requested stock.
     */
    public Cursor fetchAmountBySymbol(final String symbol) throws SQLException {
    	
        final Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{
        																  ROWID, AMOUNT}, 
            				 SYMBOL + "='" + symbol + "'", null, null, null, null, null);
        if (mCursor != null) {
        	
            mCursor.moveToFirst();
        }
        return mCursor;
    } 
    
    /**
     * Just returns the previous price of the old stock.
     */
    public Cursor fetchOldPriceBySymbol(final String symbol) throws SQLException {
    	
        final Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{
        														        ROWID, CURPRICE}, 
            				 SYMBOL + "='" + symbol + "'", null, null, null, null, null);
        if (mCursor != null) {
        	
            mCursor.moveToFirst();
        }
        return mCursor;
    } 
}