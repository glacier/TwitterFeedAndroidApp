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
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class XLBootcampActivity extends ListActivity {
    /** Called when the activity is first created. */
	
	public static final String URI = "http://search.twitter.com/search.json?q=%23bieber";
	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
	private static final String TAG = "XLBootcamp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);

        // grab tweets from twitter
        
        //what is entity?
        //I think this is passing in parameters to the base URI
//        final HttpEntity entity;
//        try {
//        	entity = new UrlEncodedFormEntity(params);
//        } catch (final UnsupportedEncodingException e) {
//        	throw new IllegalStateException(e);
//        }
        final HttpResponse response;
        final HttpGet get = new HttpGet(URI);
        ArrayList<String> tweetMessageList = new ArrayList<String>();
        
        try {
        	Log.d(TAG, "Http get from Twitter search api");
        	response = getHttpClient().execute(get);
        	
        	//parse data returned via the response object
        	Log.d(TAG, "Parse data from the response");
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        	StringBuilder builder = new StringBuilder();
        	
        	for (String line = null; (line = reader.readLine()) != null;) {
        	    builder.append(line).append("\n");
        	}
        	
        	JSONTokener tokener = new JSONTokener(builder.toString());
        	
        	//create a JSONObject so that I get get values using keys
        	try {
				JSONObject jsonObj = new JSONObject(tokener);
				//create an array of Strings of just tweet messages
				JSONArray results = jsonObj.getJSONArray("results");
				Log.d(TAG, "json results from twitter:" + results.toString());
//				for each result
//					create a JSONObject representing the tweet
//					get only the message (text field in the json response)
//					append it to a list of Java strings
				for(int i=0; i<results.length(); i++) {
					JSONObject aTweet = results.getJSONObject(i);
					String message = aTweet.getString("text");
					tweetMessageList.add(message);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				Log.e(TAG, "JSONException problem creating JSONObject from tokener:", e1);
			}
        	
//        	try {
//				JSONArray finalResult = new JSONArray(tokener);
//				
//				//parse into a array of strings
//				
//				//code to test whether the get call was made properly
////				String jsonString = finalResult.toString();
////				Log.d(TAG, "pulled json data");
////				Log.d(TAG, jsonString);
//			} catch (JSONException e) {
//				Log.e(TAG, "JSONException", e);
//			}
        } catch (final IOException e) {
        	Log.e(TAG, "IOException", e);
        	Log.d(TAG, "IOException: Message: " + e.getMessage());
        } finally {
        	Log.v(TAG, "Complete");
        }
        
//        String[] countries = getResources().getStringArray(R.array.countries_array);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, tweetMessageList));
    }
    
    // creates and returns an http client object to call twitter search
    public static HttpClient getHttpClient() {
    	HttpClient httpClient = new DefaultHttpClient();
//    	final HttpParams params = httpClient.getParams();
//        HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
//        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
//        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
    	return httpClient;
    }
    
}