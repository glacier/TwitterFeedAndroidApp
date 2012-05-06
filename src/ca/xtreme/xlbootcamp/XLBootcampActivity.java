package ca.xtreme.xlbootcamp;

import java.util.ArrayList;

import android.app.ListActivity;
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

//    private class DownloadTweetTask extends AsyncTask<Object, Integer, ArrayList<Tweet>> {

//
//		@Override
//    	protected ArrayList<Tweet> doInBackground(Object... obj){
//    		return twitter.getTimelineUpdates();
//    	}
//
//    	@Override
//    	protected void onPostExecute(ArrayList<Tweet> result) {
//    		ListView list = (ListView) findViewById(android.R.id.list);
//    		//TODO add list "push down" animation
////    		SimpleCursorAdapter adapter = new SimpleCursorAdapter(XLBootcampActivity.this, R.layout.list_item, result, TwitterUpdater.FROM, TwitterUpdater.TO);
//    		TwitterArrayAdapter adapter = new TwitterArrayAdapter(XLBootcampActivity.this, R.layout.list_item, result);
//    		list.setAdapter(adapter);
//    	}
//    }

	public void onClick(View v) {
		Log.d(TAG, "Refreshed feed");
//		new DownloadTweetTask().execute();
	}
}








