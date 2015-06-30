package com.helpercode.stockpro;

import java.io.Serializable;

/**
 * This class allows the user to create Stock objects that are used to display information
 * via our custom StockAdapter.
 * 
 * @author Brian Dennis
 * @version 9-11-2014
 */
public class Stock implements Serializable, Comparable<Object> {

	private static final long serialVersionUID = 1L;
	
	/** The symbol of stock. */
	protected String mySymbol;
	/** The quantity of stock. */
	private int myAmount;
	/** The percentage of stock. */
	private String myPercentage;

	/** The symbol of stock. */
	private String myName;
	/** The symbol of stock. */
	private String myPrice;
	/** The symbol of stock. */
	private String myHigh;
	/** The symbol of stock. */
	private String myLow;
	/** The symbol of stock. */
	private String myVolume;
	/** The symbol of stock. */
	private String myChange;
	/** The symbol of stock. */
	private String myDateAndTime;
	/** The symbol of stock. */
	private String myBeforePrice;
	
	/**
	 * Constructor that initializes variables.
	 * 
	 * @param theSymbol symbol
	 * @param theAmount amount
	 * @param thePercentage percentage
	 * @param thePrice price
	 * @param theName name
	 * @param theHigh high
	 * @param theLow low
	 * @param theVolume volume
	 * @param theChange change
	 * @param theDateAndTime date and time
	 * @param theBeforePrice previous price
	 */
	public Stock(final String theSymbol, final int theAmount, final String thePercentage,
				     final String theName, final String thePrice, final String theHigh, 
				     final String theLow, final String theVolume, final String theChange, 
				     final String theDateAndTime, final String theBeforePrice) {
		
		mySymbol = theSymbol;
		myAmount = theAmount;
		myPercentage = thePercentage;
		myName = theName;
		myPrice = thePrice;
		myHigh = theHigh;
		myLow = theLow;
		myVolume = theVolume;
		myChange = theChange;
		myDateAndTime = theDateAndTime;
		myBeforePrice = theBeforePrice;
	}

	/**
	 * @return the symbol of the stock
	 */
	public String getMySymbol() {
		return mySymbol;
	}

	/**
	 * @return how many stocks of this type we have
	 */
	public int getMyAmount() {
		return myAmount;
	}

	/**
	 * @return the percentage of stock
	 */
	public String getMyPercentage() {
		return myPercentage;
	}

	/**
	 * @return the current price of stock
	 */
	public String getMyPrice() {
		return myPrice;
	}

	/**
	 * 
	 * @return the name of the company
	 */
	public String getMyName() {
		return myName;
	}

	/**
	 * @return the high price
	 */
	public String getMyHigh() {
		return myHigh;
	}

	/**
	 * @return the low price
	 */
	public String getMyLow() {
		return myLow;
	}

	/**
	 * @return the volume of the stock
	 */
	public String getMyVolume() {
		return myVolume;
	}

	/**
	 * @return the change in the stock
	 */
	public String getMyChange() {
		return myChange;
	}

	/**
	 * @return the current date and time for when the stock was last updated
	 */
	public String getMyDateAndTime() {
		return myDateAndTime;
	}

	/**
	 * @return the price the stock was at previously
	 */
	public String getMyBeforePrice() {
		return myBeforePrice;
	}

	/**
	 * This method sets all the data for a single stock.
	 * 
	 * @param theSymbol symbol
	 * @param theAmount amount
	 * @param thePercentage percentage
	 * @param thePrice price
	 * @param theName name
	 * @param theHigh high
	 * @param theLow low
	 * @param theVolume volume
	 * @param theChange change
	 * @param theDateAndTime date and time
	 * @param theBeforePrice previous price
	 */
	public void setAllData(final String theSymbol, final int theAmount, 
			     final String thePercentage, final String thePrice, final String theName, 
			     	   final String theHigh, final  String theLow,final String theVolume, 
			     	   				 final String theChange, final String theDateAndTime, 
			     	   				 		    		   final String theBeforePrice) {
		this.mySymbol = theSymbol;
		this.myAmount = theAmount;
		this.myPercentage = thePercentage;
		this.myPrice = thePrice;
		this.myName = theName;
		this.myHigh = theHigh;
		this.myLow = theLow;
		this.myVolume = theVolume;
		this.myChange = theChange;
		this.myDateAndTime = theDateAndTime;
		this.myBeforePrice = theBeforePrice;
	}
	
	/**
	 * This method compares Stock object by their symbols. This is used in order to sort
	 * the Stock items alphabetically.
	 * 
	 * @param theOther the other stock to be compared
	 */
	@Override
	public int compareTo(final Object theOther) {
		
		final Stock theOtherStock = (Stock) theOther;
		return this.mySymbol.compareTo(theOtherStock.getMySymbol());
	}
	
	/**
	 * Sets the equality of the stock by comparing it's symbol.
	 * 
	 * @param theOther the other stock to be compared
	 */
	@Override
	public boolean equals(final Object theOther) {
		
		final Stock theOtherStock = (Stock) theOther;
		
		return mySymbol.equals(theOtherStock.getMySymbol()) 
											   && getClass() == theOtherStock.getClass();
	}
	
	/**
	 * Sets the hash code to enable this object's usage in hashing data structures.
	 */
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + myAmount;
		result = prime * result + ((myChange == null) ? 0 : myChange.hashCode());
		result = prime * result +((myDateAndTime == null) ? 0 : myDateAndTime.hashCode());
		result = prime * result + ((myHigh == null) ? 0 : myHigh.hashCode());
		result = prime * result + ((myLow == null) ? 0 : myLow.hashCode());
		result = prime * result + ((myName == null) ? 0 : myName.hashCode());
		result = prime * result + ((myPercentage == null) ? 0 : myPercentage.hashCode());
		result = prime * result + ((myPrice == null) ? 0 : myPrice.hashCode());
		result = prime * result + ((mySymbol == null) ? 0 : mySymbol.hashCode());
		result = prime * result + ((myVolume == null) ? 0 : myVolume.hashCode());
		return result;
	}
	
	/**
	 * returns a string representation of the stock's info.
	 */
	public String toString() {
		
		final StringBuilder sb = new StringBuilder();
		sb.append(mySymbol + ": ");
		sb.append(myAmount + ", ");
		sb.append(myPercentage + ", ");
		sb.append(myName + ", ");
		sb.append(myPrice + ", ");
		sb.append(myHigh + ", ");
		sb.append(myLow + ", ");
		sb.append(myVolume + ", ");
		sb.append(myChange + ", ");
		sb.append(myDateAndTime);
		
		return sb.toString();
	}
}