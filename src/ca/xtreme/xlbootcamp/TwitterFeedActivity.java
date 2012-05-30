package ca.xtreme.xlbootcamp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;


public class TwitterFeedActivity extends ListActivity implements OnClickListener {

	private TwitterClient twitter;
	private Handler mHandler = new Handler();
	private Timer mTimer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);

		twitter = new TwitterClient(this, "bieber");

		//Add a timer to check for new tweets every 30 seconds
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TweetTimerTask(), 0, 30000);
	}

	public void onClick(View v) {
			mTimer.cancel();
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
		Bundle extras = data.getExtras();
		String searchString = extras.getString("ca.xtreme.xlbootcamp.Hashtag");
		if (searchString == null) {
			searchString = "bieber";
		}
		twitter = new TwitterClient(this, searchString);
		
		new DownloadTweetTask().execute();
	}

	private class TweetTimerTask extends TimerTask {
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				public void run() {
					new DownloadTweetTask().execute();	
				}
			});
		}
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
					new TwitterSimpleCursorAdapter(TwitterFeedActivity.this, R.layout.list_item, result, 
							TwitterClient.FROM, 
							TwitterClient.TO,
							twitter);
			list.setAdapter(adapter);
			list.startLayoutAnimation();
		}
	}
}








