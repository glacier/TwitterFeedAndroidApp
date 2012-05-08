package ca.xtreme.xlbootcamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class HashtagEditActivity extends Activity {

	private Object mOriginalContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();

		// Do some setup based on the action being performed.

		final String action = intent.getAction();
		Log.d("Edit", "Received intent action" + action);
		
//		if (Intent.ACTION_EDIT.equals(action)) {
//			// Requested to edit: set that state, and the data being edited.
//			mState = STATE_EDIT;
//			mUri = intent.getData();
//		} else {
//			// Whoops, unknown action!  Bail.
//			Log.e(TAG, "Unknown action, exiting");
//			finish();
//			return;
//		}
//
//		// Set the layout for this activity.  You can find it in res/layout/note_editor.xml
		setContentView(R.layout.hashtag_edit);
//
//		// The text view for our note, identified by its ID in the XML file.
//		mText = (EditText) findViewById(R.id.note);
//
//		// Get the note!
//		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
//
//		// If an instance of this activity had previously stopped, we can
//		// get the original text it started with.
//		if (savedInstanceState != null) {
//			mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
//		}
	}
	
	protected void onStop() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		TextView textView = (TextView) findViewById(R.id.editText1);
		intent.putExtra("hashtag", textView.getText());
		setResult(0, intent);
		super.onStop();
	}
}
