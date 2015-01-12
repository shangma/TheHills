package info.shangma.thehills.voice.command;

import info.shangma.thehills.AcknowledgementPresentActivity;
import info.shangma.thehills.Application;
import info.shangma.thehills.R;
import info.shangma.thehills.mapoutside.GetPlace;
import info.shangma.thehills.mapoutside.LocationActivity;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;
import info.shangma.utils.string.Inflector;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;
import com.google.android.gms.maps.model.LatLng;

import root.gast.speech.text.WordList;
import root.gast.speech.text.match.WordMatcher;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
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
    
    private final static int MAX_LENGTH_FOR_KEYWORD = 3;
    private AlchemyAPI alchemyObj;
	AndroidHttpClient client;
	private AsyncTask<String, Void, Void> mTask;
	Inflector mInflector;
	private String targetKeyword = null;
	
	private GetPlace placeTask;
    
    public PlaceCommand(Context context, VoiceActionExecutor executor)
    {
    	this.mContext = context;
        this.executor = executor;
        this.placePrompt = context.getResources().getString(R.string.responseForPlace);
        this.matcher = new WordMatcher(context.getResources().getStringArray(R.array.placeCommand));
        
        alchemyObj = AlchemyAPI.GetInstanceFromString("f54e554a09119e3cb6e5c8485118b1a31736e996");
    	mInflector = Inflector.getInstance();        
    }
    
    @Override
    public boolean interpret(WordList heard, float [] confidence)
    {
		
        boolean understood = false;
        
        if (heard.getSource().length() > this.MAX_LENGTH_FOR_KEYWORD ) {
			mTask = new SimpleHttpGetTask();
			try {
				mTask.execute(heard.getSource());
				mTask.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.d(TAG, "finish keyword checking");

			
			if (targetKeyword != null) {
				
		        Location currentLocation = ((Application)this.mContext.getApplicationContext()).getCurrentLocation();				

				try {
					placeTask = new GetPlace(this.mContext, targetKeyword, LocationActivity.PLACE_WITH_KEYWORD_SEARCH, currentLocation);
					placeTask.execute();
					placeTask.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (placeTask.findAnyPlaces()) {
					executor.speak(placePrompt);
					understood = true;
					
					((Application)this.mContext.getApplicationContext()).setFoundPlaces(placeTask.getFoundPlaces());
					
					String firstRevised = targetKeyword.toLowerCase().replace(" ", "+");
					StringBuilder secondRevised = new StringBuilder();
					secondRevised.append("\"").append(firstRevised).append("\"");
					final String finalString = secondRevised.toString();
					
					Log.d(TAG, "final search keyword: " + finalString);
					
					Timer timer_1 = new Timer();

					timer_1.schedule(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Intent intent = new Intent(PlaceCommand.this.mContext,LocationActivity.class);
							intent.putExtra(LocationActivity.CURRENT_PLACE, finalString);
							intent.putExtra(LocationActivity.CURRENT_PLACE_TYPE, LocationActivity.PLACE_WITH_KEYWORD_SEARCH);
							((SpeechRecognitionLauncher) PlaceCommand.this.mContext).startActivity(intent);
							((SpeechRecognitionLauncher) PlaceCommand.this.mContext).finish();
						}
					}, 1000);
					
			        return understood;
				}
			} 
		}
        
        /*
		int which = matcher.isInAt(heard.getWords());
        if (which != -1)
        {
            executor.speak(placePrompt);
            understood = true;
            final String matchedString = (String) matcher.getWords().toArray()[which];
            final int placeType = 0;
            Log.d(TAG, "I understand places: " + matchedString);
                        
            Timer timer_1 = new Timer();
            
			timer_1.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent intent = new Intent(PlaceCommand.this.mContext, LocationActivity.class);
					intent.putExtra(LocationActivity.CURRENT_PLACE, matchedString);
					intent.putExtra(LocationActivity.CURRENT_PLACE_TYPE, placeType);
					((SpeechRecognitionLauncher)PlaceCommand.this.mContext).startActivity(intent);
					((SpeechRecognitionLauncher)PlaceCommand.this.mContext).finish();
				}
			}, 200);
		}
		*/
		
        Log.d(TAG, "understood is: " + understood);
        return understood;
    }
    
    private static String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
    
    private class SimpleHttpGetTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... passed) {
			// TODO Auto-generated method stub
			
			String heardSentence = passed[0];
			String currKeyword = null;
			
			try {
				Log.d(TAG, "Send out for keyword: " + heardSentence);
				Document doc = alchemyObj.TextGetRankedKeywords(heardSentence);
				doc.getDocumentElement().normalize();
				NodeList nodeList = doc.getElementsByTagName("results");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element firstElement = (Element) node;
						NodeList firstList = firstElement
								.getElementsByTagName("keyword");
						if (firstList.getLength() > 0) {
							Node node2 = firstList.item(0);
							if (node2.getNodeType() == Node.ELEMENT_NODE) {
								Element secondElement = (Element) node;
								NodeList keyList = secondElement
										.getElementsByTagName("text");
								currKeyword = keyList.item(0).getTextContent();
								targetKeyword = currKeyword;
								return null;
							}
						} else {
							Log.d(TAG, "No keyword found");
							Log.d(TAG, getStringFromDocument(doc));
						}

					}

				}

			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
    	
    }
}
