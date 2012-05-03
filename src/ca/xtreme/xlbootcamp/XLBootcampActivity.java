package ca.xtreme.xlbootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class XLBootcampActivity extends ListActivity implements OnClickListener {
    /** Called when the activity is first created. */
	

	public static final String URI = "http://search.twitter.com/search.json?q=%23bieber";
	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
	private static final String TAG = "XLBootcamp";
	
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
    private ArrayList<Tweet> getTweets(String uri) {
    	ArrayList<Tweet> tweetMessageList = new ArrayList<Tweet>();
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
    		
    		//parse and extract the data in JSON
    		JSONTokener tokener = new JSONTokener(builder.toString());

    		//create a JSONObject so that I can get values corresponding to keys
    		try {
    			JSONObject jsonObj = new JSONObject(tokener);

    			//create an array of Strings of just tweet messages
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
    	// todo learn about HttpEntity and related classes 

    	HttpClient httpClient = new DefaultHttpClient();
    	// TODO figure out how to fix invalid cookie header?
    	// But this doesn't seem to affect the API calls
    	httpClient.getParams().setParameter( ClientPNames.COOKIE_POLICY,
    			CookiePolicy.RFC_2965 );
    	
    	return httpClient;
    }

    // todo learn about java generics and how to parameterize asynctask properly
    private class GetFromTwitterTask extends AsyncTask<String, Integer, ArrayList<Tweet>> {
    	@Override
    	protected ArrayList<Tweet> doInBackground(String... uri){
    		//pull tweets from twitter
    		return getTweets(uri[0]);
    	}

    	@Override
    	protected void onPostExecute(ArrayList<Tweet> result) {
    		//populate the list view container with tweets
    		//todo learn why I can get the context this in this way.
    		ListView list = (ListView) findViewById(android.R.id.list);
    		list.setAdapter(new TwitterArrayAdapter(XLBootcampActivity.this, R.layout.list_item, result));
    	}
    }

	public void onClick(View v) {
		// refresh feed when the button is clicked
		Log.d(TAG, "Refreshed feed");
		new GetFromTwitterTask().execute(URI);
	}
	
	//customized view object for a listview
	private class TwitterArrayAdapter extends ArrayAdapter<Tweet> {
		private ArrayList<Tweet> items;
		
		public TwitterArrayAdapter(Context context, int textViewResourceId, ArrayList<Tweet> items) {
			// important, must pass items into the super() call
			super(context, textViewResourceId, items);
			this.items = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}
			//inflate items into custom view
			Tweet twt = items.get(position);
			Log.d(TAG, "Populate view with custom tweet object");
			if (twt != null) {
				//TODO display image by url
				
				//see http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
				//http://developer.android.com/resources/tutorials/views/hello-gallery.html
				//http://developer.android.com/resources/samples/HoneycombGallery/index.html
				//http://developer.android.com/guide/topics/fundamentals/processes-and-threads.html -- example of how to download images
				//http://developer.android.com/resources/samples/XmlAdapters/src/com/example/android/xmladapters/ImageDownloader.html -- complicated but seems to have a set of examples.
				
				// I don't think this will work because at this point the image is not bound to the ImageView UI element
//				ImageView profileImage = (ImageView) v.findViewById(R.id.profile_pic);
				
				TextView userText = (TextView) v.findViewById(R.id.username);
				if(userText != null){
					userText.setText(twt.getUsername());
				}

				TextView timeText = (TextView) v.findViewById(R.id.timestamp);
				if (timeText != null) {
					timeText.setText(twt.getTimestamp());
				}
				
				TextView messageText = (TextView) v.findViewById(R.id.tweet_content);
				if(messageText != null) {
					messageText.setText(twt.getTweetContent());
				}
			}
			
			return v;
		}
	}
}








