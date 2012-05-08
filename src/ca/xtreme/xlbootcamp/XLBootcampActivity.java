package ca.xtreme.xlbootcamp;

import java.io.UnsupportedEncodingException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class XLBootcampActivity extends ListActivity implements OnClickListener {
    /** Called when the activity is first created. */

	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
	private static final String TAG = "XLBootcamp";
	private TwitterUpdater twitter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //find button and attach a listener to it
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
        try {
			twitter = new TwitterUpdater(this, "#bieber");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //populate with feeds when app is launched
        //TODO Add a timer to run this task every 30 seconds to load tweet
//        new DownloadTweetTask().execute();
    }

    private class DownloadTweetTask extends AsyncTask<Object, Integer, Cursor> {
		@Override
    	protected Cursor doInBackground(Object... obj){
			Cursor c = twitter.getTimelineUpdates();
			startManagingCursor(c);
			return c;
    	}

    	@Override
    	protected void onPostExecute(Cursor result) {
    		ListView list = (ListView) findViewById(android.R.id.list);
    	       
    		//TODO add list "push down" animation
    		TwitterSimpleCursorAdapter adapter = 
    				new TwitterSimpleCursorAdapter(XLBootcampActivity.this, R.layout.list_item, result, 
    												TwitterUpdater.FROM, 
    												TwitterUpdater.TO
    											   );
    		list.setAdapter(adapter);
    	}
    }

	public void onClick(View v) {
		Log.d(TAG, "Updating Twitter timeline ...");
		new DownloadTweetTask().execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	// Launch activity to insert a new item
    	startActivityForResult(new Intent(Intent.ACTION_EDIT), 1);
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 0) {		
    		if(resultCode == 0) {
    			startActivity(new Intent(data));
    		}
    	}
    }
    
    public void onResume(Intent data) {
    	Intent intent = getIntent();
        String searchString = intent.getStringExtra("hashtag");
        if (searchString == null) {
        	searchString = "#bieber";
        }
        
        try {
			twitter = new TwitterUpdater(this, searchString);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Log.d(TAG, "hash tag search string " + searchString);
        new DownloadTweetTask().execute();
        
    }
}








