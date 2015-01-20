package info.shangma.thehills.voice.command;

import info.shangma.thehills.AcknowledgementPresentActivity;
import info.shangma.thehills.Application;
import info.shangma.thehills.R;
import info.shangma.thehills.map.inside.InsideDirectionActivity;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;
import info.shangma.thehills.voice.util.CommonUtil;

import java.util.Timer;
import java.util.TimerTask;

import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Greg Milette &#60;<a href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 *
 */
public class StartCommand implements VoiceActionCommand
{
	private final static String TAG = "StartCommand";

    private VoiceActionExecutor executor;
    private String startPrompt;
    private WordMatcher matcher;
    private Context mContext;
    
    private final static int MAX_LENGTH_FOR_KEYWORD = 3; // 1 out of 2
    
    public StartCommand(Context context, VoiceActionExecutor executor)
    {
    	this.mContext = context;
        this.executor = executor;
        this.startPrompt = context.getResources().getString(R.string.responseForStart);
        this.matcher = new WordMatcher(context.getResources().getStringArray(R.array.startCommand));
    }
    
    @Override
    public boolean interpret(WordList heard, float [] confidence)
    {
        boolean understood = false;
        
        int which = WordMatcher.NOT_IN;
        
        if (heard.getNumberOfWord() < MAX_LENGTH_FOR_KEYWORD) {
            Log.d(TAG, "echo source: " + heard.getSource());
            
            which = matcher.isInAt(heard.toString().toLowerCase());
            
            if (which != WordMatcher.NOT_IN) {
            	understood = true;
            	((Application)this.mContext.getApplicationContext()).SendMessage(CommonUtil.MOVE_COMMAND);
            	
                String matchedString = heard.toString().toLowerCase();
                Log.d(TAG, "found string: " + matchedString);
                String[] nameKeyword = matchedString.split("\\s");
                StringBuilder imageNameBuilder = new StringBuilder();
                for (String partName : nameKeyword) {
					imageNameBuilder.append(partName);
					imageNameBuilder.append('_');
				}
                
    			String toSay = String.format(startPrompt, matchedString); // donot use the one with "_"
                executor.speak(toSay);
                ((SpeechRecognitionLauncher)StartCommand.this.mContext).finish();
                
                /*
                
                Timer timer_1 = new Timer();			
    			timer_1.schedule(new TimerTask() {
    				
    				@Override
    				public void run() {
    					// TODO Auto-generated method stub
    					Intent intent = new Intent(StartCommand.this.mContext, SpeechRecognitionLauncher.class);

    					((SpeechRecognitionLauncher)StartCommand.this.mContext).finish();
    				}
    			}, 3000);
    			*/

                return understood;
			}
        }
        which = matcher.isInAt(heard.getLowercase());
		Log.d(TAG, "which is: " + which);
        
        if (which != WordMatcher.NOT_IN)
        {
            final String matchedString = (String) matcher.getWords().toArray()[which];

			String toSay = String.format(startPrompt, matchedString);
            executor.speak(toSay);
            
            understood = true;
            
            Log.d(TAG, "I understand inside location: " + matchedString);
            
            Timer timer_1 = new Timer();			
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent intent = new Intent(StartCommand.this.mContext, InsideDirectionActivity.class);
					intent.putExtra(InsideDirectionActivity.DISPLAY_THIS_IMAGE, matchedString);
					StartCommand.this.mContext.startActivity(intent);
					((SpeechRecognitionLauncher)StartCommand.this.mContext).finish();
				}
			}, 3000);
        }
        return understood;
    }
}
