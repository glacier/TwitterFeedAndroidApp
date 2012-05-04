package ca.xtreme.xlbootcamp;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwitterArrayAdapter extends ArrayAdapter<Tweet> {
	
	public TwitterArrayAdapter(Context context, int textViewResourceId, ArrayList<Tweet> items) {
		super(context, textViewResourceId, items);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		//inflates the view for a row in list view
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
		}
		
		//display tweet items into a row view
		Tweet twt = getItem(position);
		ImageView profilePic = (ImageView) v.findViewById(R.id.profile_pic);
		
		if (twt != null) {
			String imageUrl = twt.getProfilePic();
			//tag the image view with the corresponding image url that it should display
			profilePic.setTag(imageUrl);	
			new DownloadImageTask(profilePic).execute(imageUrl);

			TextView userText = (TextView) v.findViewById(R.id.username);
			if(userText != null){
				userText.setText(twt.getUsername());
			}

			TextView timeText = (TextView) v.findViewById(R.id.timestamp);
			if (timeText != null) {
				timeText.setText(twt.getTimestamp());
			}
			
			TextView messageText = (TextView) v.findViewById(R.id.tweet_content);
			if(messageText != null) {
				messageText.setText(twt.getTweetContent());
			}
		}
		
		return v;
	}
}
