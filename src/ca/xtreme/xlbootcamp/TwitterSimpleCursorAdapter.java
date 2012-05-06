package ca.xtreme.xlbootcamp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TwitterSimpleCursorAdapter extends SimpleCursorAdapter {

	public TwitterSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		// TODO Auto-generated constructor stub
	}
	
//	public TwitterSimpleCursorAdapter(Context context, Cursor c) {
//		super(context, R.layout.list_item, c, TwitterUpdater.FROM, TwitterUpdater.TO);
//	}
//	
//	@Override
//	public void bindView(View row, Context context, Cursor cursor) {
//		super.bindView(row, context, cursor);
//		
//		ImageView imageView = (ImageView) row.findViewById(R.id.profile_pic);
//		//TODO retrieve from cache
//		Bitmap image = null;
//		imageView.setImageBitmap(image);
//	
//		//bind rest of the views
//		for(int i=0; i<TwitterUpdater.TO.length; i++) {
//			TextView textView = (TextView) row.findViewById(TwitterUpdater.TO[i]);
//			String text = cursor.getString(i);
//			textView.setText(text);
//		}
//	}
}

