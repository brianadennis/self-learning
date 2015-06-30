package com.activities.stockpro;

import com.javacode.stockpro.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This class inflates the settings paged outline in res/xml/preferences.
 * 
 * @author Brian Dennis
 * @version 9-11-2014
 */
public class ManagePreferencesActivity extends PreferenceActivity {

	@Override
    public void onCreate(final Bundle theSavedInstanceState) { 
		
        super.onCreate(theSavedInstanceState);        
        addPreferencesFromResource(R.xml.preferences); 
    }

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_preferences, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
