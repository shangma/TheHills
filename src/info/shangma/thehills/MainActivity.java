package info.shangma.thehills;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.wowwee.robome.RoboMe;

import info.shangma.thehills.event.WebViewActivity;
import info.shangma.thehills.map.outside.LocationActivity;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;
import info.shangma.thehills.voice.util.CommonUtil;
import info.shangma.thehills.voice.util.ConnectToServerThread;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;

import com.gc.materialdesign.views.Switch;
import com.gc.materialdesign.views.Switch.OnCheckListener;

public class MainActivity extends Activity implements OnInitListener {
	
	private final static String TAG = "MainActivity";
	private static final String ONE_X_BT_ADDRESS = "A0:F4:50:6E:B5:63";
	private static final String NEXUS_FIVE = "50:55:27:60:5F:02";
	private static final String NEXUS_FIVE_2ND = "BC:F5:AC:9B:99:B7";
	
	public static RoboMe roboMe;
	
	private Switch serviceSwitch;
	private Switch bleSwitch;
	private TextToSpeech mTTS;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_main);
		serviceSwitch = (Switch) findViewById(R.id.serviceSwitch);
		bleSwitch = (Switch) findViewById(R.id.bleSwitch);
		
		serviceSwitch.setOncheckListener(new OnCheckListener() {
			
			@Override
			public void onCheck(boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
	                Intent i = DetectionService.makeStartServiceIntent(MainActivity.this);
	                MainActivity.this.startService(i);
				} else {
					Intent i = DetectionService.makeStopServiceIntent(MainActivity.this);
	                MainActivity.this.stopService(i);
				}
			}
		});
		
		bleSwitch.setOncheckListener(new OnCheckListener() {
			
			@Override
			public void onCheck(boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					((Application)MainActivity.this.getApplicationContext()).SendMessage(CommonUtil.MOVE_COMMAND);
				} else {
					((Application)MainActivity.this.getApplicationContext()).SendMessage(CommonUtil.MOVE_COMMAND);
				}
			}
		});
		
		mTTS = new TextToSpeech(this, this);
		
        if (!enableBluetoothComm()) {
			speakWords("Bluetooth is not available.");
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        Intent i = DetectionService.makeStartServiceIntent(MainActivity.this);
        MainActivity.this.startService(i);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		
		Intent i = DetectionService.makeStopServiceIntent(MainActivity.this);
        MainActivity.this.stopService(i);
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
	
	private void speakWords(String speech) {
		mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	public boolean enableBluetoothComm() {

		// ---if you are already talking to someone...---
		if (((Application) this.getApplicationContext()).connectToServerThread != null) {
			try {
				// ---close the connection first---
				((Application) this.getApplicationContext()).connectToServerThread.bluetoothSocket
						.close();
			} catch (IOException e) {
				Log.d("MainActivity", e.getLocalizedMessage());
			}
		}

		// ---connect to the selected Bluetooth device---
		Set<BluetoothDevice> pairedDevices = ((Application) this
				.getApplicationContext()).bluetoothAdapter.getBondedDevices();
		Log.d(TAG, "the size of paired: " + pairedDevices.size());
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.d(TAG, "name: " + device.getName() + " | Address: "
						+ device.getAddress());
				if (device.getAddress().equals(NEXUS_FIVE)
						|| device.getAddress().equals(NEXUS_FIVE_2ND)) {

					((Application) this.getApplicationContext()).connectToServerThread = new ConnectToServerThread(
							device,
							((Application) this.getApplicationContext()).bluetoothAdapter);
					((Application) this.getApplicationContext()).connectToServerThread
							.start();

					Log.d(TAG, "Connected to: " + device.getName());
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isNetworkConnectionAvailable() {  
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = cm.getActiveNetworkInfo();     
	    if (info == null) return false;
	    State network = info.getState();
	    return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		// check network availability
		if (!isNetworkConnectionAvailable()) {
			speakWords("Network is not available!");
			
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					MainActivity.this.finish();
				}
			}, 3000);
		}
	}
}
