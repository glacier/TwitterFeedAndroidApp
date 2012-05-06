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
	
	static class ViewHolder {
		TextView username;
		TextView tweetMessage;
		TextView timestamp;
		ImageView profilePic;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		//inflate a new row if its not a recycled view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
			
			holder = new ViewHolder();
//			holder.profilePic = (ImageView) convertView.findViewById(R.id.profile_pic);
			holder.username =  (TextView) convertView.findViewById(R.id.username);
			holder.tweetMessage = (TextView) convertView.findViewById(R.id.tweet_content);
			holder.timestamp =  (TextView) convertView.findViewById(R.id.timestamp);

			//Store the holder object inside the View
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		//display a tweet item into a row view
		Tweet twt = getItem(position);
		holder.username.setText(twt.getUsername());
		holder.timestamp.setText(twt.getTimestamp());
		holder.tweetMessage.setText(twt.getTweetContent());
		
		return convertView;
	}
}

