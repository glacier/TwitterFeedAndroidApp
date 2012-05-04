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
import android.os.AsyncTask;
import android.util.Log;


public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
	private static final String TAG = "XLBootcamp";
	
	protected Bitmap doInBackground(String... uri){
		//download a image given a uri
        final HttpGet getRequest = new HttpGet(uri[0]);
		Bitmap bitmap = null;

        try {
        	final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + uri);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            
            if (entity != null) {
				// download image and set it into the image view
				InputStream inputStream = entity.getContent();
				bitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
            }
            client.close();
        } catch (IOException e) {
			Log.d(TAG, "Problem downloading image" + e);
		} finally {
        }
        
		return bitmap;
	}
}