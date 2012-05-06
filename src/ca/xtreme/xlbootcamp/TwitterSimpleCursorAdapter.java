package ca.xtreme.xlbootcamp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TwitterSimpleCursorAdapter extends SimpleCursorAdapter {
	
	public TwitterSimpleCursorAdapter(Context context, Cursor c) {
		super(context, R.layout.list_item, c, Twitter.FROM, Twitter.TO);
	}
	
	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		super.bindView(row, context, cursor);
		
		ImageView imageView = (ImageView) row.findViewById(R.id.profile_pic);
		//TODO retrieve from cache
		Bitmap image = null;
		imageView.setImageBitmap(image);
	
		//bind rest of the views
		for(int i=0; i<Twitter.TO.length; i++) {
			TextView textView = (TextView) row.findViewById(Twitter.TO[i]);
			String text = cursor.getString(i);
			textView.setText(text);
		}
	}
}

