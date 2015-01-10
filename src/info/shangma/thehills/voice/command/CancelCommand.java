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
public class CancelCommand implements VoiceActionCommand
{
	private final static String TAG = "CancelCommand";

    private VoiceActionExecutor executor;
    private String cancelledPrompt;
    private WordMatcher matcher;
    private Context mContext;
    
    public CancelCommand(Context context, VoiceActionExecutor executor)
    {
    	this.mContext = context;
        this.executor = executor;
        this.cancelledPrompt = context.getResources().getString(R.string.responseForCancellation);
        this.matcher = new WordMatcher(context.getResources().getStringArray(R.array.cancelCommand));
    }
    
    @Override
    public boolean interpret(WordList heard, float [] confidence)
    {
        boolean understood = false;
        if (matcher.isIn(heard.getWords()))
        {
            executor.speak(cancelledPrompt);
            understood = true;
            
            Log.d(TAG, "I understand cancel!");
            
            Timer timer_1 = new Timer();			
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					CancelCommand.this.mContext.startActivity(new Intent(CancelCommand.this.mContext, AcknowledgementPresentActivity.class));
					((SpeechRecognitionLauncher)CancelCommand.this.mContext).finish();
				}
			}, 2000);
			
        }
        return understood;
    }
}