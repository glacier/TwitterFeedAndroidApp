package ca.xtreme.xlbootcamp;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TwitterSimpleCursorAdapter extends SimpleCursorAdapter {

	private BitmapDownloader imageCacher;
	
	public TwitterSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		imageCacher = new BitmapDownloader();
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		super.bindView(row, context, cursor);

		ImageView imageView = (ImageView) row.findViewById(R.id.profile_pic);
		String userId = cursor.getString(cursor.getColumnIndex("userid"));
		
		//lazy load images
		String imageUrl = cursor.getString(cursor.getColumnIndex("photo_url"));
		imageCacher.displayImage(imageView, imageUrl, context.getCacheDir().getAbsolutePath() + '/' + userId + ".jpg");

		TextView textView = (TextView) row.findViewById(R.id.username);
		setText(cursor, textView, "username");

		textView = (TextView) row.findViewById(R.id.tweet_content);
		setText(cursor, textView, "message");

		textView = (TextView) row.findViewById(R.id.timestamp);
		setText(cursor, textView, "timestamp");
	}

	private void setText(Cursor cursor, TextView textView, String column) {
		String text = cursor.getString(cursor.getColumnIndex(column));
		textView.setText(text);
	}
	

}

