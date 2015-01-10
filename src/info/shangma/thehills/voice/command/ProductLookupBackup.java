package info.shangma.thehills.voice.command;

import info.shangma.thehills.voice.util.CommonUtil;
import info.shangma.utils.string.Inflector;
import info.shangma.thehills.Application;
import info.shangma.thehills.R;
import info.shangma.thehills.voice.SpeechRecognitionLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogRecord;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;

import root.gast.speech.text.WordList;
import root.gast.speech.tts.TextToSpeechUtils;
import root.gast.speech.voiceaction.AbstractVoiceAction;
import root.gast.speech.voiceaction.MultiCommandVoiceAction;
import root.gast.speech.voiceaction.VoiceAction;
import root.gast.speech.voiceaction.VoiceActionCommand;
import root.gast.speech.voiceaction.VoiceActionExecutor;
import root.gast.speech.voiceaction.WhyNotUnderstoodListener;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class ProductLookupBackup implements VoiceActionCommand
{
    private static final String TAG = "Product Lookup";
    private static final String ON_DONE_PROMPT_TTS_PARAM = "ON_DONE_PROMPT";
    
    private VoiceActionExecutor executor;
    private Context context;
    
	private String url = "http://128.195.204.85/robot/response.jsp?query="; 
//	private AlchemyAPI alchemyObj;

	private boolean lookupResult;
	
	private String currentProduct;
	
	public String[] catalog = {"none",
								"seasonal", "outdoor and barbecue", "extension", "light", "vacuum",
								"kitchen", "houseware", "houseware", "storage", "cleaning",
								"bird and garden", "penst control", "connector", "drain and plumbing", "faucet",
								"silicon and paint", "painting", "miscellaneous", "tools", "safety",
								"electrical", "auto", "hanger and lock", "miscellaneous", "nut and screw"};
	

	
	AndroidHttpClient client;
	
	private AsyncTask<String, Void, Void> mTask;
	
	Inflector mInflector;

    public ProductLookupBackup(Context context, VoiceActionExecutor executor)
    {
        this.context = context;
        this.executor = executor;
		
//        alchemyObj = AlchemyAPI.GetInstanceFromString("f54e554a09119e3cb6e5c8485118b1a31736e996");
    	lookupResult = false;
    	mInflector = Inflector.getInstance();
    }
    
    
    public boolean interpret(WordList heard, float[] confidence)
    {
    	lookupResult = false;
	
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
    	
    	if (lookupResult) {
    	}
    	return lookupResult ;
    }
    
	private void prompt(String promptText) {
		Log.d(TAG, promptText);
		((SpeechRecognitionLauncher)this.context).getTts().speak(promptText,
				TextToSpeech.QUEUE_FLUSH,
				TextToSpeechUtils.makeParamsWith(ON_DONE_PROMPT_TTS_PARAM));
	}
    
	private static String convertStreamToString(InputStream is) {
		try {
			return new java.util.Scanner(is, "UTF-8").useDelimiter("\\A")
					.next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
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
	
	private String getStringRevised(String beforeRevised)
	{
		String afterRevised = mInflector.singularize(beforeRevised).replace(' ', '&');
			
		return afterRevised;
	}

    private class SimpleHttpGetTask extends AsyncTask<String, Void, Void> {
		

		@Override
		protected Void doInBackground(String... passed) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
		}
	}
    
}
