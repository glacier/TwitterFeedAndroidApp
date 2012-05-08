package ca.xtreme.xlbootcamp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class BitmapDownloader {
	// Taken from http://android-developers.blogspot.ca/2010/07/multithreading-for-performance.html
	// TODO put downloading into an async task
	public static Bitmap downloadBitmap(String uri) {
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(uri);
		
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode +
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
