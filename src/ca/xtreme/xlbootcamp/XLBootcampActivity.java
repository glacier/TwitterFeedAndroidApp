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
import android.widget.Button;
import android.widget.ListView;

public class XLBootcampActivity extends ListActivity implements OnClickListener {
    /** Called when the activity is first created. */
	
	public static final String URI = "http://search.twitter.com/search.json?q=%23bieber&rpp=100";
	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
	private static final String TAG = "XLBootcamp";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //find button and attach a listener to it
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
        //populate with feeds when app is launched
        //TODO Add a timer to run this task every 30 seconds to load tweet
        new GetFromTwitterTask().execute(URI);
    }
    
    // Gets and parses the latest tweets given a uri Twitter search API
    private ArrayList<Tweet> getTweets(String uri) {
    	ArrayList<Tweet> tweetMessageList = new ArrayList<Tweet>();
    	final HttpResponse response;
    	final HttpGet get = new HttpGet(uri);

    	try {
    		response = getHttpClient().execute(get);
    		
    		//read and serialize JSON data into a string
    		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    		StringBuilder builder = new StringBuilder();

    		for (String line = null; (line = reader.readLine()) != null;) {
    			builder.append(line).append("\n");
    		}
    		
    		//parse and extract JSON data
    		JSONTokener tokener = new JSONTokener(builder.toString());
    		try {
    			JSONObject jsonObj = new JSONObject(tokener);
    			JSONArray results = jsonObj.getJSONArray("results");

    			for(int i=0; i<results.length(); i++) {
    				JSONObject aTweet = results.getJSONObject(i);
    				
    				String tweetContent = aTweet.getString("text");
    				String timestamp = aTweet.getString("created_at");
    				String profilePic = aTweet.getString("profile_image_url");
    				String username = aTweet.getString("from_user_name");
    			
    				tweetMessageList.add(new Tweet(username, tweetContent, timestamp, profilePic));
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
    	
    	Log.d(TAG, "number of tweets saved = " + tweetMessageList.size());
    	
    	return tweetMessageList;
    }

    private HttpClient getHttpClient() {
    	HttpClient httpClient = new DefaultHttpClient();
    	return httpClient;
    }

    private class GetFromTwitterTask extends AsyncTask<String, Integer, ArrayList<Tweet>> {
    	@Override
    	protected ArrayList<Tweet> doInBackground(String... uri){
    		//pull tweets from twitter
    		return getTweets(uri[0]);
    	}

    	@Override
    	protected void onPostExecute(ArrayList<Tweet> result) {
    		ListView list = (ListView) findViewById(android.R.id.list);
    		
    		//TODO add list "push down" animation
    		list.setAdapter(new TwitterArrayAdapter(XLBootcampActivity.this, R.layout.list_item, result));
    	}
    }

	public void onClick(View v) {
		Log.d(TAG, "Refreshed feed");
		new GetFromTwitterTask().execute(URI);
	}
	
}








