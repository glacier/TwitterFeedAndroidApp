package ca.xtreme.xlbootcamp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

// TODO may not be needed once data is directly bound to the view via some content provider data retrieval mechanism
// Downloads an image in a background task and binds the image with an ImageView
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private ImageView imageView;
	private String imageUrl;
	
	public DownloadImageTask(ImageView imageView) {
		this.imageView = imageView;
	}
	
	protected Bitmap doInBackground(String... uri) {
		imageUrl = uri[0];
		// TODO fix this
		return BitmapDownloader.downloadBitmap(imageUrl);
	}
	
	protected void onPostExecute(Bitmap result) {
		String imageTag = imageView.getTag().toString();
		
		//TODO not sure about the logic here ...
		
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