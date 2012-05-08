package ca.xtreme.xlbootcamp;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
        
        twitter = new TwitterUpdater(this);
        
        //find button and attach a listener to it
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
        //populate with feeds when app is launched
        //TODO Add a timer to run this task every 30 seconds to load tweet
//        new GetFromTwitterTask().execute(URI);
        
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
}








