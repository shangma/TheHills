package info.shangma.thehills.voice.command;

import info.shangma.thehills.AcknowledgementPresentActivity;
import info.shangma.thehills.R;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;

import java.util.Timer;
import java.util.TimerTask;

import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class PlaceCommand implements VoiceActionCommand
{
	private final static String TAG = "PlaceCommand";
    private VoiceActionExecutor executor;
    private String placePrompt;
    private WordMatcher matcher;
    private Context mContext;
    
    public PlaceCommand(Context context, VoiceActionExecutor executor)
    {
    	this.mContext = context;
        this.executor = executor;
        this.placePrompt = context.getResources().getString(R.string.responseForPlace);
        this.matcher = new WordMatcher(context.getResources().getStringArray(R.array.placeCommand));
    }
    
    @Override
    public boolean interpret(WordList heard, float [] confidence)
    {
		
        boolean understood = false;
        int which = matcher.isInAt(heard.getWords());
        if (which != -1)
        {
            executor.speak(placePrompt);
            understood = true;
            
            Log.d(TAG, "I understand places: " + matcher.getWords().toArray()[which]);
            
            Timer timer_1 = new Timer();
            
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					PlaceCommand.this.mContext.startActivity(new Intent(PlaceCommand.this.mContext, AcknowledgementPresentActivity.class));
					((SpeechRecognitionLauncher)PlaceCommand.this.mContext).finish();
				}
			}, 2000);
			
        }
        return understood;
    }
}
