package com.activities.stockpro;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.helpercode.stockpro.Constants;
import com.helpercode.stockpro.Stock;
import com.helpercode.stockpro.StockAdapter;
import com.helpercode.stockpro.StockCheckingService;
import com.helpercode.stockpro.StockDBAdapter;
import com.helpercode.stockpro.StockFetcher;
import com.javacode.stockpro.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Outlines the functions of this View. screen #1 of StockPro. Lets the user input a 
 * stock and the quantity of the stock they want. This app then searches for the stock
 * online adds it to it's stock portfolio. the portfolio is displayed on the screen.
 * 
 * @author Brian Dennis
 * @version 9-1-2014
 */
public class MainScreenActivity extends Activity implements OnClickListener {
	
	/** The integer that represents how often our service will run, default of 
	 * 5000 milliseconds(5 seconds). This changes if the number is changed via the
	 * ManagePreferenceActivity class */
	public static int REFRESH_SPEED = 5000;
	
	//our view components.
	/** The name of this Class to be used in the Log commands. */
	private final String THIS_CLASS_NAME = this.getClass().getSimpleName();
	/** The ListView that holds the list of stocks from item_rows.xml. */
	private ListView myListView;
	/** The EditText that allows a user to input a stock symbol. */
	private EditText myEditTextSymbol;
	/**The EditText that allows a user to input how many stocks of the symbol they want.*/
	private EditText myEditTextQty;
	/** The button that allows a user to add the stock detailed in the EditText fields.*/
	private Button myAddButton;
	
	//our booleans used.
	/** A boolean for checking if our stocks have been accessed before or not. */
	private boolean thisIsFirstTimeFetch = true;
	/**A boolean for checking if the "More" button on screen #2(StockDetailScreenActivity)
	 * is visible or not. */
	protected static boolean moreIsNotHiding = true;
	/** A boolean to check if the program is shutting down so it can stop the service. */
	private static boolean isShuttingDown = false;
	/** A boolean for reporting if we have stocks in our SQLite database or not. */
	public static boolean iHaveStocks = false;
	/** A boolean for reporting if our settings want us to check online for data or not.*/
	private boolean isCheckingForData = true;
	
	//our private data structures.
	/** The ArrayList used to store our stocks which is then in turn used as a parameter 
	 * for "myStocksAdapter". The StockAdapter is set into "myListView", to allow the 
	 * ListView to display the information stored in "myStockList".*/
	private List<Stock> myStockList;
	/** A cursor is an indexed structure where each index stores key and value pairs of 
	 * information which are associated with a particular stock. 
	 * (Treat a cursor like a list of HashMaps). This Cursor is used to store our entire
	 * database of stocks, and is reassigned whenever our data needs to be filled again.*/
	private Cursor myStocksCursor;
	
	//for our custom adapters.
	/** The reference to the adapter for our SQLite database which allows us to retrieve 
	 * our stock information from it. */
	public static StockDBAdapter myDbHelper;
	/** the reference to our custom StockAdapter which we use to define the functionality 
	 * of how our ListView items look and function. We install this into our ListView and
	 * make a myStocksAdapter.notifyDataSetChange() call whenever that ListView needs to
	 * be updated. */
	private StockAdapter myStocksAdapter;

	//for services and receiver main thread.
	/** Our receiver used to listen for "com.javacode.action.STOCKSERVICE" messages sent 
	 * from our StockCheckingService.class. This receiver is defined at the bottom of 
	 * this class. */
	private LocalStockReceiver myReceiver;
	/** A component used to start our StockCheckingService.class */
	protected static ComponentName myComponentForService;
	/** The Intent that stores the action to start our StockCheckingService.class. */
	protected static Intent myService;
	
	// for refresh ImageButton functions
	/** A component used to start our StockCheckingService.class. this will start a 
	 * separate thread of the same service class for refreshing data. */
	protected ComponentName myComponentForRefreshService;
	/** The Intent that stores the action to start our StockCheckingService.class. This 
	 * Intent is solely used for when the user presses the "refresh" ImageButton located
	 * in this activity. */
	private Intent myRefreshService;
	/** A timer used to pause code in this class for a few seconds to allow 
	 * myRefreshService enough time to update our stock list. */
	private Timer myTimer;
	
	@Override
	protected void onCreate(final Bundle theSavedInstanceState) {
		
		super.onCreate(theSavedInstanceState);
		setContentView(R.layout.activity_main_screen);
		
		/*I had to add this to get around exception:
		 *android.os.NetworkOnMainThreadException at 
		 *android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1145)*/
		if (android.os.Build.VERSION.SDK_INT > 9) {
			
		    final StrictMode.ThreadPolicy policy = 
		    				   new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		//instantiate our List of stocks
		myStockList = new ArrayList<Stock>();
		//instantiate View components
		final ImageButton settingsIcon = (ImageButton) findViewById(R.id.settings);
		final ImageButton refreshIcon = (ImageButton) findViewById(R.id.refresh);
		myAddButton = (Button) findViewById(R.id.addButton);
		myEditTextSymbol = (EditText) findViewById(R.id.enterSymbol);
		myEditTextQty = (EditText) findViewById(R.id.enterQty);
		myListView = (ListView) findViewById(R.id.my_list);
		registerForContextMenu(myListView); //not sure exactly what this registering does.
		
		//set OnClickListeners to view components
		myAddButton.setOnClickListener(this);	
		settingsIcon.setOnClickListener(this);
		refreshIcon.setOnClickListener(this);
		myEditTextSymbol.setOnClickListener(this);
		myEditTextQty.setOnClickListener(this);
		
		//create and open database.
		myDbHelper = new StockDBAdapter(this);
        myDbHelper.open();
        // method call to add stocks stored from the SQLite database into "myStockList".
        fillData();
        
        // instantiate receiver and services. register receiver with correct message.
        // start the service, and instantiate the Timer
 		final IntentFilter mainFilter = new IntentFilter("com.javacode.action.STOCKSERVICE");
 		myReceiver = new LocalStockReceiver();		
 		registerReceiver(myReceiver, mainFilter);
 		myService = new Intent(this, StockCheckingService.class);
		myComponentForService = startService(myService);
	}
	
	/**
	 * Describes behaviors for when components are clicked, using a switch case to 
	 * differentiate which component is clicked.
	 */
	@Override
	public void onClick(final View theView) {
		
		switch (theView.getId()) {
		
		case R.id.settings: //opens the preferences activity screen
			final Intent i = new Intent(MainScreenActivity.this, 
														ManagePreferencesActivity.class);
			startActivity(i);
			break;
			
		case R.id.addButton: //calls fetchStock() which adds a stock to our portfolio
			fetchStock(); 
			break;
			
		case R.id.enterSymbol: //If you double click on this EditText it clears the field
			myEditTextSymbol.setText("");
			break;
			
		case R.id.enterQty: //If you double click on this EditText it clears the field
			myEditTextQty.setText("");
			break;
			
		case R.id.refresh: //starts a new service to update our stock portfolio
			refreshStockData();
		    break;
		}
	}
	
	/**
	 * This method starts a new service to update our stock info which executes one time.
	 */
	private void refreshStockData() {
		
		if (!myStockList.isEmpty()) {
	    	  
			myRefreshService = new Intent(this, StockCheckingService.class);
			myComponentForRefreshService = startService(myRefreshService);
			Log.d(THIS_CLASS_NAME, "REFRESH service started!");
			myTimer = new Timer();
		    myTimer.schedule(new RemindTask(), 3000);
		    Toast.makeText(MainScreenActivity.this,
					  				     "refreshing stocks", Toast.LENGTH_SHORT).show();
		    
		} else {
			
			Log.d(THIS_CLASS_NAME, "There are no stocks to update.");
	    	Toast.makeText(MainScreenActivity.this,
				         "There are no stocks to update", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * The sole purpose of this class is to cancel the service that updates our stocks 
	 * when the refresh button is clicked. It does this after 3 seconds of waiting in 
	 * order to give the service's thread a chance to fully complete it's iteration.
	 * 
	 * @author Brian Dennis
	 * @version 9-11-2014
	 */
	private class RemindTask extends TimerTask {
		
	    public void run() {
	    
	      stopService(myRefreshService);
	      Log.d(THIS_CLASS_NAME, "REFRESH service ended!");
	      myTimer.cancel(); 
	    }
	}
	
	/**
	 * This is an OnClickHandler that is defined in res/layout/item_rows.xml
	 * It defines the function for when the trash ImageButton located in the ListView
	 * rows is clicked.  When clicked, the entire row which holds the associated stock
	 * item is removed from "myStockList", and the adapter is notified to make the screen
	 * update the change.
	 * 
	 * @param theView the view which holds the reference to the Stock object contained there
	 */
	public void removeStockOnClickHandler(final View theView) {
		
		final Stock stockToRemove = (Stock) theView.getTag();
		myStockList.remove(stockToRemove); //remove from the list that updates ListView
		
		if (myStockList.size() == 0) {
			
			/* if you deleted the last stock in your portfolio, 
			 * this boolean will stop the service from updating.*/
			iHaveStocks = false; 
		}
		myDbHelper.deleteStock(stockToRemove.getMySymbol()); //remove from SQLite database
		myStocksAdapter.notifyDataSetChanged();
		Log.d(THIS_CLASS_NAME, "You deleted this stock: " + stockToRemove.toString());
	}
	
	/**
	 * This method is called when a stock is entered after pressing the "Add" button.
	 */
	private void fetchStock() {
		
		final String stringQty = myEditTextQty.getText().toString().trim();
		final int quantityToAdd = setAmountToAdd(stringQty); //method call to set quantity
		
		String symbol = myEditTextSymbol.getText().toString().trim();
		
		if ((symbol != null) && (symbol.length() > 0)) { //if a symbol was inputed
			
			symbol = symbol.toUpperCase(Locale.US).replace(" ", "+");
			myAddButton.setEnabled(false);
			
			Log.d(THIS_CLASS_NAME, "adding: " + symbol + " with amount: " + quantityToAdd); //here for async??
            
			final StockFetcher stockFetcherFromWeb = new StockFetcher(symbol);
			final List<HashMap<String, String>> webReturnedStocks = //fetch from web
					   						   stockFetcherFromWeb.getStockInformation();
			
			for (final HashMap<String, String> webStock : webReturnedStocks) {
			
				final Cursor webCursorForSingleStock = 
					   myDbHelper.fetchStockBySymbol(webStock.get(Constants.KEY_SYMBOL));
				
				final int dataElementsForStock = webCursorForSingleStock.getCount();
				webCursorForSingleStock.close();
				
				if (dataElementsForStock > 0) {
				
					//if > 0, stock exists in portfolio, so we update existing stock.
					updateStock(symbol, webStock, quantityToAdd);
				
				} else {
					//the stock is not in portfolio so we add it.
					addNewStock(webStock, quantityToAdd);
				}
			}
			myAddButton.setEnabled(true);
			fillData();
			
		} else {
			
			Toast.makeText(MainScreenActivity.this,
					        "Please provide a valid symbol ", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * This method sets default Quantity to add to "1" if no amount was entered, 
	 * otherwise we parse the string for the amount desired.
	 * 
	 * @param theStringQty the String representation of the quantity to add
	 * @return quantityToAdd
	 */
	private int setAmountToAdd(final String theStringQty) {
		
		int quantityToAdd = 0;
		if ("".equals(theStringQty)) {
			
			quantityToAdd = 1;
		} else {
			
			quantityToAdd = Integer.parseInt(theStringQty);
		}
		return quantityToAdd;
	}
	
	/**
	 * This method updates the information for an existing stock in our portfolio.
	 * 
	 * @param theSymbol The symbol for the stock, we need this to fetch the previous price
	 * @param theWebStock The HashMap that contains the data for the associated stock
	 * @param theQuantityToAdd How many of this particular stock we are adding
	 */
	private void updateStock(final String theSymbol, 
			      final HashMap<String, String> theWebStock,final int theQuantityToAdd) {
		
		final Cursor sqlCursorForAmount = 
									  (Cursor) myDbHelper.fetchAmountBySymbol(theSymbol);
		
		//find how many stocks of this kind we already have.
		final int formerAmount = sqlCursorForAmount.getInt( 
			           	sqlCursorForAmount.getColumnIndexOrThrow(StockDBAdapter.AMOUNT));
		sqlCursorForAmount.close();
		
		final int newAmount = formerAmount + theQuantityToAdd;
		
		if ("N/A".equals(theWebStock.get(Constants.KEY_CHANGE))) { 
			
			Toast.makeText(MainScreenActivity.this, "Stock does not Exist",
					  										  Toast.LENGTH_SHORT).show();
		} else {
			
			final Cursor priceCursor = myDbHelper.fetchOldPriceBySymbol(
												  theWebStock.get(Constants.KEY_SYMBOL));
			// find the previous price so we can update our red/white arrows accordingly.
			final String oldPrice = priceCursor.getString(
					         priceCursor.getColumnIndexOrThrow(StockDBAdapter.CURPRICE));
			priceCursor.close();
			
			myDbHelper.updateStock(
					theWebStock.get(Constants.KEY_SYMBOL),
					theWebStock.get(Constants.KEY_NAME),
					"$" + theWebStock.get(Constants.KEY_CURPRICE),
					"(" + theWebStock.get(Constants.KEY_PRCNTCHANGE) + ")",
					theWebStock.get(Constants.KEY_VOLUME),
					theWebStock.get(Constants.KEY_CHANGE),
					"$" + theWebStock.get(Constants.KEY_HIGHPRICE),
					"$" + theWebStock.get(Constants.KEY_LOWPRICE),
					newAmount,
					theWebStock.get(Constants.KEY_DATE) + " "
												   + theWebStock.get(Constants.KEY_TIME),
					oldPrice);
				
				Toast.makeText(MainScreenActivity.this, "Stock updated",
						  									  Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * This method adds a new stock to our portfolio
	 * 
	 * @param theWebStock The HashMap that contains the data for the associated stock
	 * @param theQuantityToAdd How many of this particular stock we are adding
	 */
	private void addNewStock(final HashMap<String, String> theWebStock, 
															final int theQuantityToAdd) {
		if ("N/A".equals(theWebStock.get(Constants.KEY_CHANGE))) {
			
			Toast.makeText(MainScreenActivity.this, "Stock does not Exist",
					  										  Toast.LENGTH_SHORT).show();
		} else {
			
			myDbHelper.createStock(
					theWebStock.get(Constants.KEY_SYMBOL),
					theWebStock.get(Constants.KEY_NAME),
					"$" + theWebStock.get(Constants.KEY_CURPRICE),
					theWebStock.get(Constants.KEY_VOLUME),
					"(" + theWebStock.get(Constants.KEY_PRCNTCHANGE) + ")",
					theWebStock.get(Constants.KEY_CHANGE),
					"$" + theWebStock.get(Constants.KEY_HIGHPRICE),
					"$" + theWebStock.get(Constants.KEY_LOWPRICE),
					theQuantityToAdd, 
					"X",
					theWebStock.get(Constants.KEY_DATE) + " " 
												   + theWebStock.get(Constants.KEY_TIME),
					"$0.0");
				
				iHaveStocks = true;
				Toast.makeText(MainScreenActivity.this, "Stock added", 
															  Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * This method makes a method call to fill "myStockList" with stock objects. If this
	 * is the first time this method has been called after the application has launched, 
	 * we instantiate our custom StockAdapter and install it into our ListView. Otherwise
	 * we just update the adapter to reflect the new additions to "myStockList".
	 */
	private void fillData() {
    	
    	myStocksCursor = myDbHelper.fetchAllStocks();
        int howManyStocksWeHaveInDataBase = myStocksCursor.getCount();
        Log.d(THIS_CLASS_NAME, "How many stocks I have in fill data: " + howManyStocksWeHaveInDataBase);

        if (howManyStocksWeHaveInDataBase > 0) {
        	
        	createListOfStockObjects();
        	iHaveStocks = true;
        }
    	if (thisIsFirstTimeFetch) {
    		
            myStocksAdapter = new StockAdapter(this, R.layout.item_rows, myStockList);
            
            /*sets the adapter to the ListView so we can see the stock items appear 
             * on screen.*/
            myListView.setAdapter(myStocksAdapter);
            thisIsFirstTimeFetch = false;
            
    	} else {
    		
    		myStocksAdapter.notifyDataSetChanged();
    	}
    }
	
	/**
	 * This method creates Stock objects out of the data retrieved from our SQLite
	 * database. We then add the Stock objects to "myStockList", then the fillData()
	 * method can finish it's task and update "myStocksAdapter" to update the view on the
	 * android screen.
	 */
	private void createListOfStockObjects() {
		
		do {
			final String symbol = myStocksCursor.getString(
							myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.SYMBOL));
			final int amount = Integer.parseInt(myStocksCursor.getString(
						   myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.AMOUNT)));
			final String percentage = myStocksCursor.getString(
						myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.PERCENTAGE));
			final String name = myStocksCursor.getString(
							  myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.NAME));
			final String price = myStocksCursor.getString(
						  myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.CURPRICE));
			final String high = myStocksCursor.getString(
							  myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.HIGH));
			final String low = myStocksCursor.getString(
							   myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.LOW));
			final String volume = myStocksCursor.getString(
							myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.VOLUME));
			final String change = myStocksCursor.getString(
							myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.CHANGE));
			final String dateAndTime = myStocksCursor.getString(
					 myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.DATE_AND_TIME));
			final String oldPrice = myStocksCursor.getString(
						 myStocksCursor.getColumnIndexOrThrow(StockDBAdapter.OLD_PRICE));
				
			final Stock singleStock = new Stock(symbol, amount, percentage, 
				          name, price, high, low, volume, change, dateAndTime, oldPrice);

			if (myStockList.contains(singleStock)) { //if stock exists, we just update
			
				myStockList.get(myStockList.indexOf(singleStock)).setAllData(
						    		      symbol, amount, percentage, price,  name, high, 
						    		      low, volume, change, dateAndTime, oldPrice);
			} else {
				
				myStockList.add(singleStock);
			}	
		}
		while (myStocksCursor.moveToNext());
		myStocksCursor.close();
	}
	
	/**
	 * This method is called whenever MainScreenActivity(this class) is returned back to
	 * the view. (So when returning back to this screen from settings or any other screen)
	 */
	@Override
	protected void onResume() {
		
		super.onResume();
		Log.d(THIS_CLASS_NAME + "onResume()", "we just switched back to " +
				"		 											MainScreenActivity");
		if (!isShuttingDown) { //if the program isn't shutting down, we care about this
			
			//check to see if settings changed
			final SharedPreferences sharedPrefs = 
									 PreferenceManager.getDefaultSharedPreferences(this);
			
			final StringBuilder builder = new StringBuilder();
	        builder.append("Enable 'More' button = "
	        			           + sharedPrefs.getBoolean("enable_more", true) + ", ");
	        builder.append("The current theme = "
	        			        + sharedPrefs.getString("theme_choice", "light") + ", ");
	        builder.append("isCheckingForData = "
	        			        + sharedPrefs.getBoolean("check_for_data", true) + ", ");
	        builder.append("The refresh interval = "
	        			            + sharedPrefs.getString("refresh_interval", "5000")); 
	        Log.d(THIS_CLASS_NAME, "Settings are: " + builder.toString());
	        
	        //set the correct fields and do something with them
	        moreIsNotHiding = sharedPrefs.getBoolean("enable_more", true);
	        REFRESH_SPEED = Integer.valueOf(sharedPrefs.getString(
	        												"refresh_interval", "5000"));
	        isCheckingForData = sharedPrefs.getBoolean("check_for_data", true);
	        if (isCheckingForData) {
	        	
	        	stopService(myService); //I don't want to risk multiple threads existing.
	        	myComponentForService = startService(myService);
	        	Toast.makeText(MainScreenActivity.this, "Online Service activated", 
						  									  Toast.LENGTH_SHORT).show();
	        } else {
	        	
	        	stopService(myService);
	        	Toast.makeText(MainScreenActivity.this, "Online Service deactivated", 
						  									  Toast.LENGTH_SHORT).show();
	        }
	        Log.d(THIS_CLASS_NAME, "App was not shutting down while "
	        												  + "onResume() was called");
		}
		else {
			
			Log.d(THIS_CLASS_NAME, "App is shutting down while onResume() was called");
		}
	}
	
	/**
	 * This method is called when the back button is pressed on MainScreenActivity.
	 * (When application is shut down)
	 */
	@Override
	protected void onDestroy() {
		
		isShuttingDown = true;
		if (myStocksCursor != null) {
			
			myStocksCursor.close();
			Log.d(THIS_CLASS_NAME + ": onDestroy()", "Stock Cursor closed");
		}
		if (myDbHelper != null) {
			
			myDbHelper.close();
			Log.d(THIS_CLASS_NAME + ": onDestroy()", "Stock database adapter closed");
		}
		try {

			unregisterReceiver(myReceiver);
			stopService(myService);
			Log.d(THIS_CLASS_NAME + ": onDestroy()", 
											"SERVICE STOPPED and RECEIVER UNREGISTERED");
		} catch (Exception e) {
			
			Log.e(THIS_CLASS_NAME + "onDestroy()", "Shit went wrong!!!");
		}
		super.onDestroy();
	}
	
	/**
	 * This internal class allows the instantiation of this object, and receives broadcast
	 * messages of "com.javacode.action.STOCKSERVICE" as defined in onCreate() of
	 * MainScreenActivity.java.
	 * 
	 * @author Brian Dennis
	 * @version 9-11-2014
	 */
	private class LocalStockReceiver extends BroadcastReceiver {

		/**
		 * This method receives information from StockCheckingService.java.
		 * It retrieves the ArrayList of the List of HashMaps that represent all of the
		 * updated stocks that was extracted from the intent that was sent. It then 
		 * updates our SQLite database and makes a fillData() method call to refresh
		 * the display of our ListView.
		 */
		@Override
		public void onReceive(final Context theContext,final  Intent theIntent) {
			
			@SuppressWarnings("unchecked")
			final List<ArrayList<HashMap<String, String>>> listOfTheListOfStocks = 
											   (List<ArrayList<HashMap<String, String>>>) 
					                    theIntent.getSerializableExtra("Updated Stocks");
			
			for (final ArrayList<HashMap<String, String>> stockList :
															     listOfTheListOfStocks) {
				
				for (final HashMap<String, String> stock : stockList) {
					
					String oldPrice = "";
					if (iHaveStocks) {
						Cursor priceCursor = myDbHelper.fetchOldPriceBySymbol(
														stock.get(Constants.KEY_SYMBOL));
						
						if (priceCursor.getCount() > 0) {
							
							oldPrice = priceCursor.getString(
												       priceCursor.getColumnIndexOrThrow(
												    		   StockDBAdapter.CURPRICE));
							priceCursor.close();
						}
					}
					
					myDbHelper.refreshStocks(
							stock.get(Constants.KEY_SYMBOL),
							stock.get(Constants.KEY_NAME),
							"$" + stock.get(Constants.KEY_CURPRICE),
							"(" + stock.get(Constants.KEY_PRCNTCHANGE) + ")",
							stock.get(Constants.KEY_VOLUME),
							stock.get(Constants.KEY_CHANGE),
							"$" + stock.get(Constants.KEY_HIGHPRICE),
							"$" + stock.get(Constants.KEY_LOWPRICE),
							stock.get(Constants.KEY_DATE) + " "
													    + stock.get(Constants.KEY_TIME),
					        oldPrice);	
				}
			}
			fillData();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu theMenu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_portfolio, theMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem theItem) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final int id = theItem.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(theItem);
	}
}