package info.shangma.thehills;

import com.wowwee.robome.RoboMe;

import info.shangma.thehills.event.WebViewActivity;
import info.shangma.thehills.map.outside.LocationActivity;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;
import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	private final static String TAG = "MainActivity";

	
	public static RoboMe roboMe;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public void onInsideEventBtn(View view) {
		Log.d(TAG, "inside event button clicked");
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.DISPLAY_THIS_URL, "http://128.195.204.85/robot/hotelmanage.jsp");
		startActivity(intent);
	}
	
	public void onOutsideEventBtn(View view) {
		Log.d(TAG, "outside event button clicked");
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.DISPLAY_THIS_URL, "http://www.meetup.com/cities/us/ca/laguna_hills/");
		startActivity(intent);
	}
	
	public void onInsideLocationBtn(View view) {
		Log.d(TAG, "inside location button clicked");
		Intent intent = new Intent(this, SpeechRecognitionLauncher.class);
		intent.putExtra(SpeechRecognitionLauncher.TYPE_OF_LOCATION_OR_EVENT, SpeechRecognitionLauncher.INSIDE_LOCATION);
		startActivity(intent);
	}

	public void onOutsideLocationBtn(View view) {
		Intent intent = new Intent(this, SpeechRecognitionLauncher.class);
		intent.putExtra(SpeechRecognitionLauncher.TYPE_OF_LOCATION_OR_EVENT, SpeechRecognitionLauncher.OUTSIDE_LOCATION);
		startActivity(intent);
	}
}
