package info.shangma.thehills.map.inside;
import info.shangma.thehills.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class InsideDirectionActivity extends Activity {
	
	private static final String TAG = "InsideDirectionActivity";
	private String imageName;
	private ImageView insideImageView;
	
	public static final String DISPLAY_THIS_IMAGE = "info.shangma.thehills.map.inside.insidedirectionactivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inside_direction);
		
		Log.d(TAG, "ready to show the image");
		insideImageView = (ImageView) findViewById(R.id.insideDirectionView);
		imageName = getIntent().getStringExtra(this.DISPLAY_THIS_IMAGE);
		if (imageName != null) {
			int resourceId = this.getApplicationContext().getResources().getIdentifier(imageName, "drawable", this.getApplicationContext().getPackageName());
			insideImageView.setImageResource(resourceId);	
		}		
	}
}
