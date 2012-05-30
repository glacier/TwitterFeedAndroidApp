package ca.xtreme.xlbootcamp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TwitterSimpleCursorAdapter extends SimpleCursorAdapter {

	private TwitterClient twitterClient;
	
	public TwitterSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, TwitterClient twitter) {
		super(context, layout, c, from, to);
		this.twitterClient = twitter;
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		super.bindView(row, context, cursor);

		ImageView imageView = (ImageView) row.findViewById(R.id.profile_pic);
		String userId = cursor.getString(cursor.getColumnIndex("userid"));

		//lazy load images
		Bitmap image = twitterClient.getProfileImage(userId);
		if(image == null) {
			// if the image is not in the cache, download it as the view is being rendered
			String imageUrl = cursor.getString(cursor.getColumnIndex("photo_url"));
			Log.d("Simple Cursor adapter", "Downloading image at " + imageUrl);
			new DownloadImageTask(imageView).execute(imageUrl);
		} else {
			// just use cached image
			imageView.setImageBitmap(image);
		}

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
	
	// Note: not actually sure that I define an asynctask class inside a class
	// that is not an Activity class
	private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
		private ImageView imageView;
		public DownloadImageTask(ImageView imageView) {
			this.imageView = imageView;
		}
		@Override
		protected Bitmap doInBackground(String... uri){
			Bitmap image = BitmapDownloader.downloadBitmap(uri[0]);
			return image;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			//Set the image to the view
			imageView.setImageBitmap(result);
		}
	}
}

