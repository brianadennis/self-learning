package com.helpercode.stockpro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.activities.stockpro.MainScreenActivity;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

/**
 * Instantiates the service, setting the appropriate variables.
 * 
 * @author Brian Dennis
 * @version 9-11-2014
 */
public class StockCheckingService extends Service {
	
	/** Checks to see if the service should still be running. Used in case the thread is
	 * still technically running, but the applications should have terminated it on 
	 * onDestroy().
	 */
	private boolean isRunning = true;
	private final String THIS_CLASS_NAME = this.getClass().getSimpleName();

	@Override
	public IBinder onBind(final Intent arg0) {
		
		return null;
	}

	@Override
	public void onCreate() {
		
		super.onCreate();
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.d("MyStockCheckingService:onStart", "service is running");
		// we place the slow work of the service in its own thread
		// so the caller is not hung up waiting for us
		
		final Thread triggerService = new Thread(new RunThread());
		triggerService.start();
	}

	/**
	 * The repeatable thread for the Service.
	 * 
	 * @author Bad Man Dennis
	 * @version 9-11-2014
	 */
	public class RunThread implements Runnable {
	
		public void run() {
			
			while (isRunning) { 
				
				if (MainScreenActivity.iHaveStocks) { //variable that stops the service
													  // when stock list is empty
					refreshStocks();
				}
			}
		}
		
		/**
		 * This method first retrieves our stocks stored in the SQLight database and 
		 * and stores their symbols inside of an ArrayList. It then uses these symbols to
		 * fetch the current stock information on them via our StockFetcher class. The
		 * StockFetcher class returns a HashMap<String, String> of a single stock and 
		 * it's information, and we store these HashMaps inside of an ArrayList. The List
		 * is then stored inside of an Intent, and the Intent is broadcasted for any 
		 * BroadcastReceiver who is interested to pick up.
		 */
		public void refreshStocks() {
			
			// sleepTime is read from MainActivity which is updated when settings change.
			int sleepTime = MainScreenActivity.REFRESH_SPEED;
			try {
				
				final StockDBAdapter mDbHelperForService = 
													MainScreenActivity.myDbHelper;
				final Cursor myStocksCursor = mDbHelperForService.fetchAllStocks();
				final List<String> myStockSymbols = new ArrayList<String>();					
					
				do {
					final String symbolName = myStocksCursor.getString(
							myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.SYMBOL));
					myStockSymbols.add(symbolName);
				}
				while (myStocksCursor.moveToNext()); //end retrieving all stock symbol.
				
				final List<ArrayList<HashMap<String, String>>> allMyStocks =
							         new ArrayList<ArrayList<HashMap<String, String>>>();
				
				for (final String symbol : myStockSymbols) {
					
					final StockFetcher stockFetcherFromWeb = new StockFetcher(symbol);
					final List<HashMap<String, String>> webReturnedStock = 
									   stockFetcherFromWeb.getStockInformation();
					allMyStocks.add((ArrayList<HashMap<String, String>>) 
																	   webReturnedStock);
				} //end fetching current stock data from web.
				Log.d(THIS_CLASS_NAME, "Data from all of our stocks have been "
									 + "retrieved from the internet via service class.");
				
				final Intent myFilteredStockData = new Intent(
											         "com.javacode.action.STOCKSERVICE");
				myFilteredStockData.putExtra(
										   "Updated Stocks", (Serializable) allMyStocks);
				sendBroadcast(myFilteredStockData);
				Thread.sleep(sleepTime); 
				
			} catch (Exception e) {
				
				Log.e(THIS_CLASS_NAME, "Error running app to web service!");
				e.printStackTrace();
			}
		}		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("MyStockCheckingService:onDestroy()", "service destroyed from onDestroy()");
		isRunning = false;
	}
}