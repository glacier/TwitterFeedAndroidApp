package ca.xtreme.xlbootcamp;

import java.util.ArrayList;

import android.content.Context;
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
		
		//inflate a new row if its not a recycled view
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
		}
		
		//display a tweet item into a row view
		Tweet twt = getItem(position);
		ImageView profilePic = (ImageView) v.findViewById(R.id.profile_pic);
		
		String imageUrl = twt.getProfilePic();
		
		//tag the image view with the corresponding image url that it should display
		profilePic.setTag(imageUrl);	
		ImageView imageView = (ImageView) v.findViewById(R.id.profile_pic);
		new DownloadImageTask(imageView).execute(imageUrl);

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
		
		return v;
	}
}
