package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class XLBootcampActivity extends ListActivity implements OnClickListener {
    /** Called when the activity is first created. */
	
	public static final String URI = "http://search.twitter.com/search.json?q=%23bieber";
	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
	private static final String TAG = "XLBootcamp";
//	private twitterTask = new GetFromTwitterTask();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //find button and attach listener to it
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
        //populate with feeds when app is launched
        new GetFromTwitterTask().execute(URI);
    }
    
    // Gets and parses the latest tweets given a uri Twitter search API
    private ArrayList<String> getTweets(String uri) {
    	ArrayList<String> tweetMessageList = new ArrayList<String>();
    	final HttpResponse response;
    	final HttpGet get = new HttpGet(uri);

    	try {
    		Log.d(TAG, "Http get from Twitter search api");
    		response = getHttpClient().execute(get);

    		Log.d(TAG, "Parse data from the response");

    		//read and serial JSON data into a String
    		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    		StringBuilder builder = new StringBuilder();

    		for (String line = null; (line = reader.readLine()) != null;) {
    			builder.append(line).append("\n");
    		}
    		
    		Log.d(TAG, builder.toString());
    		
    		//parse and extract the data in JSON
    		JSONTokener tokener = new JSONTokener(builder.toString());

    		//create a JSONObject so that I can get values corresponding to keys
    		try {
    			JSONObject jsonObj = new JSONObject(tokener);

    			//create an array of Strings of just tweet messages
    			JSONArray results = jsonObj.getJSONArray("results");

    			//Log.d(TAG, "json results from twitter:" + results.toString());

    			for(int i=0; i<results.length(); i++) {
    				JSONObject aTweet = results.getJSONObject(i);
    				String message = aTweet.getString("text");
    				tweetMessageList.add(message);
    			}
    		} catch (JSONException e) {
    			Log.e(TAG, "JSONException problem creating JSONObject from tokener:", e);
    		}
    	} catch (final IOException e) {
    		Log.e(TAG, "IOException", e);
    		Log.d(TAG, "IOException: Message: " + e.getMessage());
    	} finally {
    		Log.v(TAG, "Complete");
    	}

    	return tweetMessageList;
    }

    private HttpClient getHttpClient() {
    	// todo learn about HttpEntity and related classes 
    	HttpClient httpClient = new DefaultHttpClient();
    	return httpClient;
    }

    // todo learn about java generics and how to parameterize asynctask properly
    private class GetFromTwitterTask extends AsyncTask<String, Integer, ArrayList<String>> {
    	@Override
    	protected ArrayList<String> doInBackground(String... uri){
    		//pull tweets from twitter
    		return getTweets(uri[0]);
    	}

    	@Override
    	protected void onPostExecute(ArrayList<String> result) {
    		//populate the list view container with tweets
    		//todo learn why I can get the context this in this way.
    		ListView list = (ListView) findViewById(android.R.id.list);
    		list.setAdapter(new ArrayAdapter<String>(XLBootcampActivity.this, R.layout.list_item, result));
    	}
    }

	public void onClick(View v) {
		// refresh feed when the button is clicked
		Log.d(TAG, "Refreshed feed");
		new GetFromTwitterTask().execute(URI);
	}
}







