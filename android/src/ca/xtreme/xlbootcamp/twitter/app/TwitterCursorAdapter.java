package ca.xtreme.xlbootcamp.twitter.app;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ca.xtreme.xlbootcamp.twitter.R;

public class TwitterCursorAdapter extends CursorAdapter {

	private static final String TAG = "TwitterCursorAdapter"; 
	private BitmapDownloader imageCacher;
	
	public TwitterCursorAdapter(Context context, int layout, Cursor c) {
		super(context, c);
		imageCacher = new BitmapDownloader();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Log.d(TAG, "Getting bindView for item " + cursor.getPosition());
		
		String imageUrl = cursor.getString(cursor.getColumnIndex("photo_url"));
		ImageView imageView = (ImageView) view.findViewById(R.id.profile_pic);

		// Fetch the image
		String userId = cursor.getString(cursor.getColumnIndex("userid"));
		imageCacher.displayImage(imageView, imageUrl, 
				context.getCacheDir().getAbsolutePath() + '/' + userId + ".jpg");
		
		//Set the rest of the views
		TextView textView = (TextView) view.findViewById(R.id.username);
		setText(cursor, textView, "username");

		textView = (TextView) view.findViewById(R.id.tweet_content);
		setText(cursor, textView, "message");

		textView = (TextView) view.findViewById(R.id.timestamp);
		setText(cursor, textView, "timestamp");
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.d(TAG, "newView is called for " + cursor.getPosition());
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.list_item, parent, false);

		return view;
	}
	
	private void setText(Cursor cursor, TextView textView, String column) {
		String text = cursor.getString(cursor.getColumnIndex(column));
		textView.setText(text);
	}
}

