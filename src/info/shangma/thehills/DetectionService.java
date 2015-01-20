package info.shangma.thehills;


import info.shangma.thehills.event.WebViewActivity;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;

import java.util.List;

import root.gast.speech.SpeechRecognitionUtil;
import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class DetectionService extends Service implements RecognitionListener {

	private static final String TAG = "Detection Service";

	public static final String ACTIVATION_STOP_INTENT_KEY = "ACTIVATION_STOP_INTENT_KEY";
    public static final String ACTIVATION_TYPE_INTENT_KEY = "ACTIVATION_TYPE_INTENT_KEY";

	private static final String TARGETWORDS = "hello";

	private boolean isStarted;

	private SpeechRecognizer recognizer;

	private WordMatcher matcher;
	private final static int MAX_LENGTH_FOR_KEYWORD = 3;
	private String matchedString;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		isStarted = false;

		this.matcher = new WordMatcher(this.getResources().getStringArray(R.array.startCommand));;

		if (recognizer == null) {
			recognizer = SpeechRecognizer
					.createSpeechRecognizer(DetectionService.this);
		}
	}
	
	
    @Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "The service is destroyed");
	}


	public static Intent makeStartServiceIntent(Context context)
    {
        Intent i = new Intent(context, DetectionService.class);
        return i;
    }

    public static Intent makeStopServiceIntent(Context context)
    {
        Intent i = new Intent(context, DetectionService.class);
        i.putExtra(ACTIVATION_STOP_INTENT_KEY, true);
        return i;
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		if (intent != null) {

			if (intent.hasExtra(ACTIVATION_STOP_INTENT_KEY)) {

				Log.d(TAG, "stop service intent");
				stopDeteciotn();
			} else {

				if (isStarted) {
					Log.i(TAG, "Service already started");
				}
				startDetection();

			}
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void startDetection() {

		Log.i(TAG, "Detection started");
		isStarted = true;

		Intent recognizerIntent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		// accept partial results if they come
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

		recognizer.setRecognitionListener(this);
		recognizer.startListening(recognizerIntent);

	}

	private void stopDeteciotn() {
		isStarted = false;
		stopSelf();
	}

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int errorCode) {
		// TODO Auto-generated method stub
		if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH)
				|| (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
			Log.d(TAG, "didn't recognize anything");
			// keep going
			startDetection();
		} else {
			Log.d(TAG,
					"FAILED "
							+ SpeechRecognitionUtil
									.diagnoseErrorCode(errorCode));
		}
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub
		Log.d(TAG, "partial results");
//		receiveResults(partialResults);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub
		Log.d(TAG, "ready for speech " + params);

	}

	@Override
	public void onResults(Bundle results) {
		// TODO Auto-generated method stub
		Log.d(TAG, "full results");
		receiveResults(results);
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub
//		Log.d(TAG, "Rms Changed");
	}

	private void receiveResults(Bundle results) {

		if ((results != null)
				&& results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
			List<String> heard = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] scores = results
					.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			
			Log.d(TAG, "the size is: " + heard.size());
			for (int i = 0; i < heard.size(); i++) {
				Log.d(TAG, heard.get(i) + " ");
			}
			
			if (heard.size() != 0) {
				receiveWhatWasHeard(heard, scores);
			}
		} else {
			Log.d(TAG, "no results");
		}
	}

	private void receiveWhatWasHeard(List<String> heard, float[] scores) {
		boolean heardTargetWord = false;
		
		
		// find the target word
		for (String possible : heard) {
//            Log.d(TAG, "echo source: " + possible);

			WordList wordList = new WordList(possible);
			
	        int which = WordMatcher.NOT_IN;
	        
	        if (wordList.getNumberOfWord() < MAX_LENGTH_FOR_KEYWORD) {
	            
	            which = matcher.isInAt(wordList.toString().toLowerCase());
	            
	            if (which != WordMatcher.NOT_IN) {
	            	heardTargetWord = true;
	            	
	                matchedString = wordList.toString().toLowerCase();
	                Log.d(TAG, "found string: " + matchedString);
	                break;
	                
	           }
	        }
		}

		if (heardTargetWord) {
			Log.d(TAG, "Ready to stop");
			this.activated(true);
		} else {
			// keep going
			startDetection();
		}
	}

	private void stop() {

		if (recognizer != null) {
			recognizer.stopListening();
			recognizer.cancel();
			recognizer.destroy();
		}

	}

	private void activated(boolean success) {
		stop();

		// broadcast result
//		Intent intent = new Intent(SpeechActivationBroadcastReceiver.ACTIVATION_RESULT_BROADCAST_NAME);
//		intent.putExtra(SpeechActivationBroadcastReceiver.ACTIVATION_RESULT_INTENT_KEY, success);
//		sendBroadcast(intent);
		
		
		// start the launch directly
        Intent intent = new Intent();
        
        if (matchedString.equals("inside event")) {
    		intent.setClass(this, WebViewActivity.class);
    		intent.putExtra(WebViewActivity.DISPLAY_THIS_URL, "http://128.195.204.85/robot/hotelmanage.jsp");
			
		} else if (matchedString.equals("inside location")) {
			
			intent.setClass(this, SpeechRecognitionLauncher.class);
			intent.putExtra(SpeechRecognitionLauncher.TYPE_OF_LOCATION_OR_EVENT, SpeechRecognitionLauncher.INSIDE_LOCATION);
			
		} else if (matchedString.equals("outside event")) {
			intent.setClass(this, WebViewActivity.class);
			intent.putExtra(WebViewActivity.DISPLAY_THIS_URL, "http://www.meetup.com/cities/us/ca/laguna_hills/");
			
		} else if (matchedString.equals("outside location")) {
			intent.setClass(this, SpeechRecognitionLauncher.class);
			intent.putExtra(SpeechRecognitionLauncher.TYPE_OF_LOCATION_OR_EVENT, SpeechRecognitionLauncher.OUTSIDE_LOCATION);
		}
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

		Log.i(TAG, "found it");
		// always stop after receive an activation
//		stopSelf();
//		startDetection();
		

	}
}
