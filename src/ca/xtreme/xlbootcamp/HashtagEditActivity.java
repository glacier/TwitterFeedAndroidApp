package ca.xtreme.xlbootcamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HashtagEditActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		final Intent intent = getIntent();

		// Do some setup based on the action being performed.

		final String action = intent.getAction();
		Log.d("Edit", "Received intent action " + action);
		
		// Set the layout for this activity.  You can find it in res/layout/note_editor.xml
		setContentView(R.layout.hashtag_edit);
		
		Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(this);
        
        
	}
	
	public void onClick(View v) {
		Intent intent = new Intent();
		TextView textView = (TextView) findViewById(R.id.editText1);
		intent.putExtra("ca.xtreme.xlbootcamp.Hashtag", textView.getText().toString());
		Log.d("HashtagActivity", "sending back hashtag " + intent.getStringExtra("ca.xtreme.xlbootcamp.Hashtag"));
		
		setResult(0, intent);
		
		finish();
	}
}
