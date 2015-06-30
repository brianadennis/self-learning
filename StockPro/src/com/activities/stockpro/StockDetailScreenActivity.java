package com.activities.stockpro;

import com.helpercode.stockpro.StockDBAdapter;
import com.javacode.stockpro.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Outlines the functions of this View. screen #1 of StockPro. Displays more detailed
 * information of the stock.
 * 
 * @author Brian Dennis
 * @version 9-1-2014
 */
public class StockDetailScreenActivity extends Activity implements OnClickListener {

	/** The bundle of stock information used to pass up to the next screen. */
	Bundle myBundle;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_detail_screen);
		
		stopService(MainScreenActivity.myService);
		
		final Button moreButton = (Button) findViewById(R.id.more);
		final Button listButton = (Button) findViewById(R.id.list);
		
		moreButton.setOnClickListener(this);
		listButton.setOnClickListener(this);
		
		if (MainScreenActivity.moreIsNotHiding) {
			
			moreButton.setVisibility(View.VISIBLE);
			
		} else {
			
			moreButton.setVisibility(View.GONE);
		}
		
		final TextView symbol = (TextView) findViewById(R.id.stockName);
		final TextView company = (TextView) findViewById(R.id.companyName);
		final TextView price = (TextView) findViewById(R.id.priceOfStock);
		final TextView volume = (TextView) findViewById(R.id.volume);
		final TextView change = (TextView) findViewById(R.id.change);
		final TextView dateAndTime = (TextView) findViewById(R.id.dateAndTime);
		final TextView high = (TextView) findViewById(R.id.weekHigh);
		final TextView low = (TextView) findViewById(R.id.weekLow);
	
		myBundle = getIntent().getExtras();
		
		dateAndTime.setText(myBundle.getString(StockDBAdapter.DATE_AND_TIME));
		symbol.setText(myBundle.getString(StockDBAdapter.SYMBOL));
		company.setText(myBundle.getString(StockDBAdapter.NAME));
		price.setText(myBundle.getString(StockDBAdapter.CURPRICE));
		volume.setText(myBundle.getString(StockDBAdapter.VOLUME));
		change.setText(myBundle.getString(StockDBAdapter.CHANGE));
		high.setText(myBundle.getString(StockDBAdapter.HIGH));
		low.setText(myBundle.getString(StockDBAdapter.LOW));
	}

	/**
	 * This method will either return to the list of stocks or go forward to the next 
	 * screen.
	 */
	@Override
	public void onClick(final View theView) {
		
		if (theView.getId() == R.id.list) { //go back to list
			
			StockDetailScreenActivity.this.onBackPressed();
			
		} else if (theView.getId() == R.id.more) { //go to the more detailed screen
			
			final Intent i = new Intent(
				    StockDetailScreenActivity.this, StockDetailMoreScreenActivity.class);
			i.putExtras(myBundle);
			startActivity(i);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stock_quote, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}