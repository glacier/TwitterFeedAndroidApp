package ca.xtreme.xlbootcamp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

// Downloads an image in a background task and binds the image with an ImageView
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private ImageView imageView;
	private String imageUrl;
	
	public DownloadImageTask(ImageView imageView) {
		this.imageView = imageView;
	}
	
	protected Bitmap doInBackground(String... uri) {
		Log.d("DownloadImageTask", "Downloading " + uri + " in doInBackground");
		imageUrl = uri[0];
		return BitmapDownloader.downloadBitmap(imageUrl);
	}
	
	protected void onPostExecute(Bitmap result) {
		String imageTag = imageView.getTag().toString();
		
		if(!imageTag.equals(imageUrl)) {
			return;
		}
		
		//set downloaded image into the image view
		if(result != null && imageView != null) {
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(result);
		} else {
			// when is the result and imageView ever gone
			imageView.setVisibility(View.GONE);
		}
	}
}