package info.shangma.thehills.voice;


import info.shangma.thehills.AcknowledgementPresentActivity;
import info.shangma.thehills.R;
import info.shangma.thehills.voice.command.CancelCommand;
import info.shangma.thehills.voice.command.InsidePlaceCommand;
import info.shangma.thehills.voice.command.OutsidePlaceCommand;
import info.shangma.thehills.voice.command.StartCommand;
import info.shangma.thehills.voice.util.SoundPoolPlayerEx;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import root.gast.speech.SpeechRecognizingAndSpeakingActivity;
import root.gast.speech.tts.TextToSpeechUtils;
import root.gast.speech.voiceaction.AbstractVoiceAction;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;

public class SpeechRecognitionLauncher extends
		SpeechRecognizingAndSpeakingActivity {
	private static final String TAG = "SpeechRecognitionLauncher";
	public static final String TYPE_OF_LOCATION_OR_EVENT = "info.shangma.thehills.locationAndevent";
	
	public static final int INVALID_TYPE = -1;
	public static final int START_EVENT = 0;
	public static final int INSIDE_EVENT = 1;
	public static final int INSIDE_LOCATION = 2;
	public static final int OUTSIDE_EVENT = 3;
	public static final int OUTSIDE_LOCATION = 4;

	private static final String ON_DONE_PROMPT_TTS_PARAM = "ON_DONE_PROMPT";

	private VoiceActionExecutor executor;

	private VoiceAction hotleVoiceAction;
	
	private SoundPoolPlayerEx mSoundPlayer;
	
	private int voiceActionType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.launcher_voice_recognition);
 	
		if (executor == null) {
			executor = new VoiceActionExecutor(this);
		}
		
		mSoundPlayer = new SoundPoolPlayerEx(this);
		executor.setSoundPlayer(mSoundPlayer);
		
		voiceActionType = getIntent().getIntExtra(this.TYPE_OF_LOCATION_OR_EVENT, -1);
		
		if (voiceActionType == this.INVALID_TYPE) {
			Log.d(TAG, "input is invalid");
		} else if (voiceActionType == this.START_EVENT) {
			Log.d(TAG, "input is for start");
			hotleVoiceAction = startVoiceAction();
		} else if (voiceActionType ==  this.OUTSIDE_LOCATION) {
			Log.d(TAG, "input is for outside location");
			hotleVoiceAction = outsidePlaceVoiceAction();
		} else if (voiceActionType == this.INSIDE_LOCATION) {
			Log.d(TAG, "input is for inside location");
			hotleVoiceAction = insidePlaceVoiceAction();
		} else if (voiceActionType == this.OUTSIDE_EVENT) {
			Log.d(TAG, "input is for outside event");
			
		} else {
			Log.d(TAG, "input is for inside event");
		}
		
		Log.i(TAG, "finish initialization");
	}

	@Override
	public void onSuccessfulInit(TextToSpeech tts) {
		super.onSuccessfulInit(tts);
		
		executor.setTts(getTts());
		
		Log.d(TAG, "Ready for the first query");
		executor.execute(hotleVoiceAction);
	}
	
	private VoiceAction startVoiceAction() {
		
		VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
		VoiceActionCommand startCommand = new StartCommand(this, executor);
		
		VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(cancelCommand, startCommand));
		voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this, executor, false));
		
		String LOOKUP_PROMPT = getResources().getString(R.string.speech_launcher_prompt);
		voiceAction.setPrompt(LOOKUP_PROMPT);
		voiceAction.setSpokenPrompt(LOOKUP_PROMPT);
		
		voiceAction.setActionType(AbstractVoiceAction.FirstVoiceActionOutofTwo);
		
		return voiceAction;
	}
	private VoiceAction insidePlaceVoiceAction() {
		Log.d(TAG, "insideLocationVoiceAction");
		boolean relaxed = false;
		
		VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
		VoiceActionCommand insidePlaceCommand = new InsidePlaceCommand(this, executor);
		
		VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(cancelCommand, insidePlaceCommand));
		voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this, executor, false));
		
		String LOOKUP_PROMPT = getResources().getString(R.string.speech_launcher_prompt);
		voiceAction.setPrompt(LOOKUP_PROMPT);
		voiceAction.setSpokenPrompt(LOOKUP_PROMPT);
		
		voiceAction.setActionType(AbstractVoiceAction.FirstVoiceActionOutofTwo);
		
		return voiceAction;
		
	}

	private VoiceAction outsidePlaceVoiceAction() {
		
		// match it with two levels of strictness
		Log.d(TAG, "outsideLocationVoiceAction");
		boolean relaxed = false;

		VoiceActionCommand cancelCommand = new CancelCommand(this, executor);
		VoiceActionCommand outsidePlaceCommand = new OutsidePlaceCommand(this, executor);

		VoiceAction voiceAction = new MultiCommandVoiceAction(Arrays.asList(cancelCommand, outsidePlaceCommand));
		// don't retry
		voiceAction.setNotUnderstood(new WhyNotUnderstoodListener(this, executor, false));
		
		String LOOKUP_PROMPT = getResources().getString(R.string.speech_launcher_prompt);
		voiceAction.setPrompt(LOOKUP_PROMPT);
		voiceAction.setSpokenPrompt(LOOKUP_PROMPT);
		
		voiceAction.setActionType(AbstractVoiceAction.FirstVoiceActionOutofTwo);

		return voiceAction;
	}

	private void prompt(String promptText) {
		Log.d(TAG, promptText);
		getTts().speak(promptText,
				TextToSpeech.QUEUE_FLUSH,
				TextToSpeechUtils.makeParamsWith(ON_DONE_PROMPT_TTS_PARAM));
	}

	/**
	 * super class handles registering the UtteranceProgressListener and calling
	 * this
	 */

	@Override
	protected void receiveWhatWasHeard(List<String> heard,
			float[] confidenceScores) {
		// satisfy abstract class, this class handles the results directly
		// instead of using this method
//		for (String word : heard) {
//			Log.d(TAG, "I heard: " + word);
//		}
		Log.d(TAG, "I just received " + heard.size());
		
		executor.handleReceiveWhatWasHeard(heard, confidenceScores);
	}

	
	@Override
	protected void recognitionFailure(int errorCode) {
		// TODO Auto-generated method stub
		super.recognitionFailure(errorCode);
		
		Log.d(TAG, "recognitionFailure in SpeechRecognitionLauncher");
		
		switch (errorCode) {
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			startActivity(new Intent(this, AcknowledgementPresentActivity.class));
			this.finish();
			break;
		
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
		case SpeechRecognizer.ERROR_NETWORK:
			prompt("Network is not right.");
			
			Timer timer_1 = new Timer();			
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SpeechRecognitionLauncher.this.finish();
				}
			}, 4000);
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			prompt("Sorry. I do not understand what you said. Would you like to try again?");
			
			Timer timer_2 = new Timer();			
			timer_2.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SpeechRecognitionLauncher.this.finish();
				}
			}, 4000);
			break;
			
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
		case SpeechRecognizer.ERROR_SERVER:
			prompt("Sorry! Could you try this service later?");
			
			Timer timer_3 = new Timer();
			timer_3.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SpeechRecognitionLauncher.this.finish();
				}
			}, 4000);
			break;
		default:
			break;
		}
	}
}
