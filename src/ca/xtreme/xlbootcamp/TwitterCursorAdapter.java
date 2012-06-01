package ca.xtreme.xlbootcamp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwitterCursorAdapter extends CursorAdapter {

	private BitmapDownloader imageCacher;
	
	public TwitterCursorAdapter(Context context, int layout, Cursor c) {
		super(context, c);
		imageCacher = new BitmapDownloader();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
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
	
	private void setText(Cursor cursor, TextView textView, String column) {
		String text = cursor.getString(cursor.getColumnIndex(column));
		textView.setText(text);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.list_item, parent, false);

		return view;
	}
}

