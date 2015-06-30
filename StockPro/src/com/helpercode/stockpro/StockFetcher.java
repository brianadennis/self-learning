package com.helpercode.stockpro;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

 
import android.text.Html;
import android.util.Log;
 
/**
 * This class fetches stock information from an online site.
 * 
 * @author Brian Dennis
 * @version 9-11-2014
 */
public class StockFetcher {
 
    String mStock;
 
    public StockFetcher(final String stockList) {
        mStock = stockList;
    }
 
    public String getXmlFromUrl(final String url) {
    	
        String xml = null;
 
        try {
            // defaultHttpClient
        	final DefaultHttpClient httpClient = new DefaultHttpClient();
            final HttpGet httpPost = new HttpGet(url);
 
            final HttpResponse httpResponse = httpClient.execute(httpPost);
            final HttpEntity httpEntity = httpResponse.getEntity();
            final String newString = EntityUtils.toString(httpEntity, "UTF-8");
            xml = Html.fromHtml(newString).toString();
 
        } catch (UnsupportedEncodingException e) {
        	
        	Log.e("StockFetcher", "UnsupportedEncodingException Error trying to reach URL");
            //e.printStackTrace();
        } catch (ClientProtocolException e) {
        	Log.e("StockFetcher", "ClientProtocolException Error trying to reach URL");
            //e.printStackTrace();
        } catch (IOException e) {
        	Log.e("StockFetcher", "IOException Error trying to reach URL. You probably "
        									  + "don't have internet access right now!");
            //e.printStackTrace();
        }
        // return XML
        return xml;
    }
 
    public List<HashMap<String, String>> getStockInformation() {
 
    	final List<HashMap<String, String>> stocks = 
    											new ArrayList<HashMap<String, String>>();
 
        if ((mStock != null) && (mStock.length() > 0)) {
        	
        	final String webAddress = 
        			"http://www.webservicex.net/stockquote.asmx/GetQuote?symbol="+ mStock;
        	final String stockXML = getXmlFromUrl(webAddress);
            if (stockXML != null) {
            	final Document doc = getDomElement(stockXML); // getting DOM element
 
            	final NodeList nl = doc.getElementsByTagName(Constants.KEY_ITEM);
 
                // looping through all item nodes <item>
                for (int i = 0; i < nl.getLength(); i++) {
 
                	final Node n = nl.item(i);
                	final Element e = (Element) n;
                	final  String symbol = getValue(e, Constants.KEY_SYMBOL);
                	final String name = getValue(e, Constants.KEY_NAME);
                	final String lastprice = getValue(e, Constants.KEY_CURPRICE);
                	final String date = getValue(e, Constants.KEY_DATE);
                	final String time = getValue(e, Constants.KEY_TIME);
                	final String change = getValue(e, Constants.KEY_CHANGE);
                	final String opening = getValue(e, Constants.KEY_OPENPRICE);
                	final String highprice = getValue(e, Constants.KEY_HIGHPRICE);
                	final String lowprice = getValue(e, Constants.KEY_LOWPRICE);
                	final String volume = getValue(e, Constants.KEY_VOLUME);
                	final String mktcap = getValue(e, Constants.KEY_MKTCAP);
                	final String prevclose = getValue(e, Constants.KEY_PREVCLOSE);
 
                	final  String percentchange = getValue(e,
                            Constants.KEY_PRCNTCHANGE);
                	final String annrange = getValue(e, Constants.KEY_ANNRANGE);
                	final String earning = getValue(e, Constants.KEY_EARNING);
                	final String pe = getValue(e, Constants.KEY_PE);
                	final HashMap<String, String> stock = createStock(symbol, name, 
                			lastprice, date, time, change, opening, highprice, lowprice,
                			volume, mktcap, prevclose, percentchange, annrange, 
                			earning, pe);
                    stocks.add(stock);
                }
 
            }
        }
        return stocks;
    }
 
    private HashMap<String, String> createStock(final String symbol,final String name,
    		final String lastprice,final String date,final  String time,
    		final String change, String opening, String highprice, String lowprice,
    		final String volume,final String mktcap,final String prevclose,
    		final String percentchange,final String annrange,final String earning,
    		final String pe) {
    	
        final HashMap<String, String> hm = new HashMap<String, String>();
        hm.put(Constants.KEY_PE, pe);
        hm.put(Constants.KEY_EARNING, earning);
        hm.put(Constants.KEY_ANNRANGE, annrange);
        hm.put(Constants.KEY_PRCNTCHANGE, percentchange);
 
        hm.put(Constants.KEY_PREVCLOSE, prevclose);
        hm.put(Constants.KEY_MKTCAP, mktcap);
        hm.put(Constants.KEY_VOLUME, volume);
        hm.put(Constants.KEY_LOWPRICE, lowprice);
        hm.put(Constants.KEY_HIGHPRICE, highprice);
        hm.put(Constants.KEY_OPENPRICE, opening);
        hm.put(Constants.KEY_CHANGE, change);
        hm.put(Constants.KEY_TIME, time);
        hm.put(Constants.KEY_DATE, date);
        hm.put(Constants.KEY_CURPRICE, lastprice);
        hm.put(Constants.KEY_NAME, name);
        hm.put(Constants.KEY_SYMBOL, symbol);
 
        return hm;
    }
 
    public Document getDomElement(final String xml) {
        Log.d("StockFetcher", "XML=" + xml);
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
        	final DocumentBuilder db = dbf.newDocumentBuilder();
 
        	final InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);
 
        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        // return DOM
        return doc;
    }
 
    public String getValue(final Element item,final String str) {
    	
    	final NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }
 
    public final String getElementValue(final Node elem) {
        Node child;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (child = elem.getFirstChild(); child != null; child = child
                        .getNextSibling()) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    } 
}