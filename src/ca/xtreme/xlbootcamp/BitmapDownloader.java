package ca.xtreme.xlbootcamp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class BitmapDownloader {

	// Launches an asynctask to populate an image into a image view
	public void displayImage(ImageView imageView, String imageUrl, String diskFilename) {
		new DownloadImageTask(imageView).execute(imageUrl, diskFilename);
	}
	
	// Note: not actually sure that I define an asynctask class inside a class
	// that is not an Activity class
	private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
		private ImageView imageView;
		public DownloadImageTask(ImageView imageView) {
			this.imageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String
				... uri){
			return getImage(uri[0], uri[1]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			//Set the image to the view
			imageView.setImageBitmap(result);
		}
	}
	
	private Bitmap getImage(String imageUrl, String diskFilename) {
		File file = new File(diskFilename);
		if(!file.exists()) {
			Log.d("Image Download", "Downloading " + imageUrl);
			return forceDownloadImage(imageUrl, file);
		}
//		Log.d("Image Download", "Retrieving from " + diskFilename);
		return BitmapFactory.decodeFile(diskFilename);
	}
	
	private Bitmap forceDownloadImage(String imageUrl, File file) {
		Bitmap image = downloadBitmap(imageUrl);

		if(image == null) {
			return null;
		}

		try {
			FileOutputStream fout = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			Log.d("TwitterUpdater", "Could not open " + file.getAbsoluteFile());
		} catch (IOException e) {
			Log.d("TwitterUpdater", "Could not compress bitmap");
		}

		return image;
	}
	
	// Taken from http://android-developers.blogspot.ca/2010/07/multithreading-for-performance.html
	private Bitmap downloadBitmap(String uri) {
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(uri);
		
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("BitmapDownloader", "Error " + statusCode +
						" while retrieving bitmap from " + uri);
				return null;
			}

			final HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					return bitmap;
				} finally {
					if(inputStream != null) {
						inputStream.close();
					}
					// why is this called?
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			// Abort request
			getRequest.abort();
			Log.d("BitmapDownloader", "Problem downloading image" + e);
		} finally {
			if(client != null) {
				client.close();
			}
		}
		return null;
	}
}
