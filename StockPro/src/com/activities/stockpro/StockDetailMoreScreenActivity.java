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
 * Outlines the functions of this View. screen #2 of StockPro.
 * It displays even more detailed information on the stock.
 * 
 * @author Brian Dennis
 * @version 9-1-2014
 */
public class StockDetailMoreScreenActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_detail_more_screen);
		
		
		final TextView myDateAndTimeView = (TextView) findViewById(R.id.dateAndTimeMore);
		final TextView mySymbolView = (TextView) findViewById(R.id.stockNameMore);
		final TextView myCompanyNameView = (TextView) findViewById(R.id.companyNameMore);
		
		final Button myListButton = (Button) findViewById(R.id.list2);
		final Button myLessButton = (Button) findViewById(R.id.less);
		
		myListButton.setOnClickListener(this);
		myLessButton.setOnClickListener(this);
		
		final Bundle myBundle = getIntent().getExtras();
		
		myDateAndTimeView.setText(myBundle.getString(StockDBAdapter.DATE_AND_TIME));
		mySymbolView.setText(myBundle.getString(StockDBAdapter.SYMBOL));
		myCompanyNameView.setText(myBundle.getString(StockDBAdapter.NAME));
	}

	/**
	 * This method will either return to the list of stocks or go back to the previous 
	 * screen.
	 */
	@Override
	public void onClick(final View theView) {
		
		if (theView.getId() == R.id.list2) { //go back to stock list
			
			Intent i = new Intent(StockDetailMoreScreenActivity.this, MainScreenActivity.class);
			startActivity(i);
			
		} else if (theView.getId() == R.id.less) { //go back to previous screen
			
			StockDetailMoreScreenActivity.this.onBackPressed();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stock_detail_more_screen, menu);
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
