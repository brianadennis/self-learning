package com.helpercode.stockpro;

import java.util.Collections;
import java.util.List;

import com.activities.stockpro.MainScreenActivity;
import com.activities.stockpro.StockDetailScreenActivity;
import com.javacode.stockpro.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class is a custom adapter that is used on our ListView from the 
 * MainScreenActivity.java. it defines what is contained in each item row, and also 
 * defines the OnClick functionality of clicking the items on the rows.
 * 
 * @author Bad Man Dennis
 * @version 9-11-2014
 */
public class StockAdapter extends ArrayAdapter<Stock> implements OnClickListener {

	/** The name of this Class to be used in the Log commands. */
	private final String THIS_CLASS_NAME = this.getClass().getSimpleName();
	
	/** The List of the stocks in this adapter. */
	private List<Stock> myStockList;
	/** ID of the resource. */
	private int myLayoutResourceId;
	/** the context. */
	private Context myContext;
	/** The Object that holds a stock and it's view containers. */
	private StockHolder myHolder;
	/** The position in the ListView. */
	private int myPosition;

	public StockAdapter(final Context theContext, final int theLayoutResourceId, 
															final List<Stock> theItems) {
		super(theContext, theLayoutResourceId, theItems);
		this.myLayoutResourceId = theLayoutResourceId;
		this.myContext = theContext;
		this.myStockList = theItems;
		myHolder = null;
	}

	/**
	 * Creates the ImageViews, ImageButton, and ImageView for a single row.
	 * It sets tag references to them, adds listeners to the TextViews, and makes a call
	 * to setUpStock() to set the text of the TextViews.
	 */
	@Override
	public View getView(final int position, View row, final ViewGroup parent) {
		
		myPosition = position;
			
		final LayoutInflater inflater = 
								    ((MainScreenActivity) myContext).getLayoutInflater();
		row = inflater.inflate(R.layout.item_rows, parent, false);
		myHolder = new StockHolder();
		myHolder.singleStock = myStockList.get(position);
		myHolder.removeStockButton = (ImageButton) row.findViewById(R.id.remove);
		myHolder.removeStockButton.setTag(myHolder.singleStock);

		myHolder.symbol = (TextView) row.findViewById(R.id.sym);
		myHolder.symbol.setTag(myHolder.singleStock);
		myHolder.amount = (TextView) row.findViewById(R.id.amount);
		myHolder.amount.setTag(myHolder.singleStock);
		myHolder.percentage = (TextView) row.findViewById(R.id.percent);
		myHolder.percentage.setTag(myHolder.singleStock);
		myHolder.name = (TextView) row.findViewById(R.id.fullName);
		myHolder.name.setTag(myHolder.singleStock);
		myHolder.price = (TextView) row.findViewById(R.id.price);
		myHolder.price.setTag(myHolder.singleStock);
		myHolder.arrow = (ImageView) row.findViewById(R.id.arrow);
		myHolder.arrow.setTag(myHolder.singleStock);
		
		myHolder.symbol.setOnClickListener(this);
		myHolder.amount.setOnClickListener(this);
		myHolder.percentage.setOnClickListener(this);
		myHolder.name.setOnClickListener(this);
		myHolder.price.setOnClickListener(this);
		myHolder.arrow.setOnClickListener(this);
		row.setTag(myHolder);
			
		//puts the visual display of stocks in alphabetical order.
		setupStockTextViews(myHolder);
		Collections.sort(myStockList);
		
		if (position % 2 == 1) {
			
 		    row.setBackgroundColor(Color.TRANSPARENT);  
 		    
 		} else {
 			
 		    row.setBackgroundColor(Color.BLACK);  
 		}
		return row;
	}

	/**
	 * This method sets the TextViews text, and determine if the price increased or 
	 * decreased and sets the arrow image accordingly.
	 * 
	 * @param theHolder the Object that holds the stock and its components.
	 */
	private void setupStockTextViews(final StockHolder theHolder) {
		
		theHolder.symbol.setText(myStockList.get(myPosition).getMySymbol());
		theHolder.amount.setText(String.valueOf(
								             myStockList.get(myPosition).getMyAmount()));
		theHolder.percentage.setText(myStockList.get(myPosition).getMyPercentage());
		theHolder.name.setText(myStockList.get(myPosition).getMyName());
		theHolder.price.setText(myStockList.get(myPosition).getMyPrice());
	
		//calculate the previous price of stock
		final String beforePriceString = myStockList.get(myPosition).getMyBeforePrice(); 
		final String stringBeforePriceNoSign = beforePriceString.substring(
														  1, beforePriceString.length());
		final double beforePrice = Double.valueOf(stringBeforePriceNoSign);
		//calculate the current price of stock
		final String stringCurrentPrice = myStockList.get(myPosition).getMyPrice();
		final String stringCurrentPriceNoSign = myStockList.get(
					  myPosition).getMyPrice().substring(1, stringCurrentPrice.length());
		final double currentPrice = Double.valueOf(stringCurrentPriceNoSign);
		
		if (currentPrice >= beforePrice) {
			
			theHolder.arrow.setImageResource((R.drawable.white_up_arrow));
			
		} else {
			
			theHolder.arrow.setImageResource((R.drawable.red_down_arrow));
		}
	}

	/**
	 * This class acts as a holder for a Stock Item and the different components use in
	 * one item row in a ListView.
	 * 
	 * @author Brian Dennis
	 * @version 9-11-2014
	 */
	private static class StockHolder {
		
		Stock singleStock;
		TextView symbol;
		TextView amount;
		TextView percentage;
		ImageButton removeStockButton;
		TextView name;
		TextView price;
		ImageView arrow;
	}

	/**
	 * Defines the function of what happens when a stock item in the list is clicked on.
	 */
	@Override
	public void onClick(View v) {
		
		final Intent i = new Intent(v.getContext(), StockDetailScreenActivity.class);
		final Stock stock = (Stock) v.getTag();
		final Bundle stockInfoBundle = new Bundle();
		stockInfoBundle.putString(StockDBAdapter.DATE_AND_TIME, stock.getMyDateAndTime());
		stockInfoBundle.putString(StockDBAdapter.SYMBOL, stock.getMySymbol());
		stockInfoBundle.putString(StockDBAdapter.NAME, stock.getMyName());
		stockInfoBundle.putString(StockDBAdapter.CURPRICE, stock.getMyPrice());
		stockInfoBundle.putString(StockDBAdapter.HIGH, stock.getMyHigh());
		stockInfoBundle.putString(StockDBAdapter.LOW, stock.getMyLow());
		stockInfoBundle.putString(StockDBAdapter.VOLUME, stock.getMyVolume());
		stockInfoBundle.putString(StockDBAdapter.CHANGE, stock.getMyChange());
		
		i.putExtras(stockInfoBundle);
		myContext.startActivity(i);
		Log.i("StockAdapter", "sending infos to StockDetailScreen");
	}
}