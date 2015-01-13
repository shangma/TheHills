package info.shangma.thehills.event;

import info.shangma.thehills.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
	
	private static final String TAG = "WebViewActivity";
	private WebView myWebView;
	
	public static final String DISPLAY_THIS_URL = "info.shangma.thehills.event.webviewactivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_meetup);
		
		myWebView = (WebView) findViewById(R.id.meetupView);
		myWebView.setWebViewClient(new MyWebViewClient());
		myWebView.getSettings().setJavaScriptEnabled(true);
		
		String myUrl = getIntent().getStringExtra(DISPLAY_THIS_URL);
		if (myUrl != null) {
			myWebView.loadUrl(myUrl);
		}		
	}

	private class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			view.loadUrl(url);
			return true;
		}
		
	}
}
