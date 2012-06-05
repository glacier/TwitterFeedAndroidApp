package ca.xtreme.xlbootcamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HashtagEditActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hashtag_edit);
		Button button = (Button) findViewById(R.id.editDoneButton);
        button.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		Intent intent = new Intent();
		TextView textView = (TextView) findViewById(R.id.hashtagText);
		
		// Pass back the input (the hashtag) via the extras field in an intent object
		intent.putExtra(Twitter.HASHTAG_INTENT, textView.getText().toString());
		setResult(0, intent);
		
		// End the activity and return the result
		finish();
	}
}
