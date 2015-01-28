package info.shangma.thehills.map.outside;

import info.shangma.thehills.Application;
import info.shangma.thehills.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;


import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.directions.route.Segment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.internal.am;
import com.google.android.gms.drive.internal.as;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class LocationActivity extends FragmentActivity implements 
							OnMapReadyCallback, OnCameraChangeListener,
							OnMarkerClickListener, OnMyLocationButtonClickListener,
							OnInfoWindowClickListener, OnInitListener,
							RoutingListener {
	
	private final static String TAG = "LocationActivity";
	public final static String CURRENT_PLACE = "info.shangma.thehills.currentPlace";
	public final static String CURRENT_PLACE_TYPE = "info.shangma.thehills.currentPlaceType";
	
	// for map
	private GoogleMap mMap;
    private LatLng theHillsLatLng = new LatLng(33.59769, -117.67853); 
    private LatLng testLatLng = new LatLng(33.66184, -117.80358);
    
    public static final CameraPosition THEHILLS =
            new CameraPosition.Builder().target(new LatLng(33.59769, -117.67853))
                    .zoom(17f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    public static final CameraPosition TEST_CAMERA_POSITION =
            new CameraPosition.Builder().target(new LatLng(33.66184, -117.80358))
                    .zoom(17f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    
    private float currentZoom;
    
    private float MAX_ZOOM_LEVEL = 21.0f;    
    private float MIN_ZOOM_LEVEL = 2.0f;
    private float PREFERRED_ZOOM_LEVEL = 17.0f;
    
    // for places

	

	private final Random mRandom = new Random();
	
    private Marker mMyLocationMarker;
    private Marker mTheHillsMarker;
    private Marker mCurrentLocationMarker;
    
    private CheckBox checkBoxAll;
    private CheckBox checkBoxATM;
    private CheckBox checkBoxBank;
    private CheckBox checkBoxPharmacy;
    private CheckBox checkBoxCafe;
    private CheckBox checkBoxRestaurant;
    private CheckBox checkBoxShopping;
    
    private ListView listPlace;
    
    private List<String> instructions;
    private ListView listInstructions;
    private TextToSpeech mTTS;			// to read the instructions
    private boolean readyToSay = false;
    private String firstToSay;
    
    private ViewSwitcher viewSwitcher;
    private  Animation slide_in_left, slide_out_right;
    
    private Map<String, MarkerOptions> mapMarkerOptions;
    
    // for routing
//    private Location destinationLocation;
    private LatLng destinationLatLng;
    
    // from caller
    
    private String targetPlaceKeyword;
    private int targetPlaceType = -1;
    
    public final static int PLACE_WITH_RIGHT_TYPE = 0;
    public final static int PLACE_WITH_KEYWORD_SEARCH =1;
    
    // 
    private GetPlace placeTask;
    private ArrayList<Place> foundPlaces;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.activity_location);
				
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		mTTS = new TextToSpeech(this, this);


		/*
		checkBoxAll = (CheckBox) findViewById(R.id.checkbox_all);
	    checkBoxATM = (CheckBox) findViewById(R.id.checkbox_atm);
	    checkBoxBank = (CheckBox) findViewById(R.id.checkbox_bank);
	    checkBoxPharmacy = (CheckBox) findViewById(R.id.checkbox_pharmacy);
	    checkBoxCafe = (CheckBox) findViewById(R.id.checkbox_cafe);
	    checkBoxRestaurant = (CheckBox) findViewById(R.id.checkbox_restaurant);
	    checkBoxShopping = (CheckBox) findViewById(R.id.checkbox_shopping);
	    */
		
	    
	    targetPlaceKeyword = getIntent().getStringExtra(this.CURRENT_PLACE);
	    if (targetPlaceKeyword != null) {
	    	targetPlaceType = getIntent().getIntExtra(this.CURRENT_PLACE_TYPE, -1);		
		}
	    

		if ((targetPlaceKeyword!= null)&&(targetPlaceType != -1)) {
			Log.d(TAG, "listview ready");
			foundPlaces = ((Application)this.getApplicationContext()).getFoundPlaces();
			PlaceListAdpater adapter = new PlaceListAdpater(foundPlaces);

			viewSwitcher = (ViewSwitcher) findViewById(R.id.viewswitcher);
			slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
			slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

			viewSwitcher.setInAnimation(slide_in_left);
			viewSwitcher.setOutAnimation(slide_out_right);

			listPlace = (ListView) findViewById(R.id.list_place);
			listInstructions = (ListView) findViewById(R.id.list_instructions);
			
			listPlace.setAdapter(adapter);
			
		}
		
		String keywordSearched = ((Application)this.getApplicationContext()).getKeywordSearched();
		String nearestStringPrompt = this.getResources().getString(R.string.say_the_nearest_options);
		String nearestKeyword = String.format(nearestStringPrompt, keywordSearched);
		
		StringBuilder toSay = new StringBuilder();
		
		toSay.append(nearestKeyword);
		toSay.append(foundPlaces.get(0).getName());
		toSay.append(" at ");
		toSay.append(foundPlaces.get(0).getVicinity());
		firstToSay = toSay.toString();
		
		Log.d(TAG, "firstToSay is: " + firstToSay);

		
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private class PlaceListAdpater extends ArrayAdapter<Place> {

		public PlaceListAdpater(ArrayList<Place> places) {
			super(getApplicationContext(), 0, places);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_place, null);
			}
			
			final Place aPlace = getItem(position);
			
			TextView titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
			titleTextView.setText(aPlace.getName());
			
			TextView snippetView = (TextView) convertView.findViewById(R.id.snipTextView);
			snippetView.setText(aPlace.getVicinity());
			
			TextView idView = (TextView) convertView.findViewById(R.id.idTextView);
			idView.setText(aPlace.getId());
			
			convertView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.d(TAG, "I got clicked: " + aPlace.getId());
					if (mapMarkerOptions.size() > 0) {
						
						showAllMarkerOnMap();
						MarkerOptions mOptions = mapMarkerOptions.get(aPlace.getId());
						LocationActivity.this.setDestinationForRouting(mOptions);
					}
				}
			});
			
			return convertView;
		}
	}
	
	
	public void onCheckboxAll(View view) {
		this.fetchTargetPlaces();
	}
	public void onCheckboxATM(View view) {
		checkBoxAll.setChecked(false);
		this.fetchTargetPlaces();
	}

	public void onCheckboxBank(View view) {
		checkBoxAll.setChecked(false);
		this.fetchTargetPlaces();
	}

	public void onCheckboxPharmacy(View view) {
		checkBoxAll.setChecked(false);

		this.fetchTargetPlaces();
	}

	public void onCheckboxcafe(View view) {
		checkBoxAll.setChecked(false);

		this.fetchTargetPlaces();
	}

	public void onCheckboxRestaurant(View view) {
		checkBoxAll.setChecked(false);

		this.fetchTargetPlaces();
	}

	public void onCheckboxShopping(View view) {
		checkBoxAll.setChecked(false);

		this.fetchTargetPlaces();
	}
	
	private void fetchTargetPlaces() {
		
		Location currentLocation = ((Application)getApplication()).getCurrentLocation();
		if (checkBoxAll.isChecked()) {
			try {
//				placeTask = new GetPlace(this, "atm|bank|pharmacy|cafe|restaurant|shopping_mall", this.PLACE_WITH_RIGHT_TYPE, currentLocation);
				placeTask.execute();
				placeTask.get();
				createTargetMarkers();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		mMap.clear();

		StringBuilder targetPlaceBuilder =  new StringBuilder();
		
		if (checkBoxATM.isChecked()) {
			targetPlaceBuilder.append("atm|");
		}
		if (checkBoxBank.isChecked()) {
			targetPlaceBuilder.append("bank|");
		}
		if (checkBoxPharmacy.isChecked()) {
			targetPlaceBuilder.append("pharmacy|");
		}
		if (checkBoxCafe.isChecked()) {
			targetPlaceBuilder.append("cafe|");
		}
		if (checkBoxRestaurant.isChecked()) {
			targetPlaceBuilder.append("restaurant|");
		}
		if (checkBoxShopping.isChecked()) {
			targetPlaceBuilder.append("shopping_mall|");
		}
		
		if (targetPlaceBuilder.length() > 0) {
			String targetString = targetPlaceBuilder.substring(0, targetPlaceBuilder.length()-1).toString();
			Log.d(TAG, "request target string: " + targetString);
			/*
			try {
				placeTask = new GetPlace(this, targetString, this.PLACE_WITH_RIGHT_TYPE, currentLocation);
				placeTask.execute();
				placeTask.get();
				displayTargetPlaces();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
	}
	
	private void createTargetMarkers() {
		
		if (foundPlaces.size() >0) {
			
			mapMarkerOptions = new TreeMap<String, MarkerOptions>();
			
			for (int i = 0; i < foundPlaces.size(); i++) {
				MarkerOptions mOptions = new MarkerOptions()
						.title("Click here to see more")//(foundPlaces.get(i).getName())
						.position(new LatLng(foundPlaces.get(i).getLatitude(), 
								foundPlaces.get(i).getLongitude()))
						.icon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360))
						.alpha((float)(mRandom.nextFloat()*0.5+0.5)) // alpha is always greater than 0.5
						.snippet(foundPlaces.get(i).getVicinity());
				mapMarkerOptions.put(foundPlaces.get(i).getId(), mOptions);
				
			}
			
			this.showAllMarkerOnMap();
			
			Log.d(TAG, "mapMarker's size is:" + mapMarkerOptions.size());
						
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(foundPlaces.get(0).getLatitude(), foundPlaces.get(0).getLongitude()))
					.zoom(14) 
					.tilt(30) 
					.build(); 
			
			mMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}
	}
	
	private void showAllMarkerOnMap() {
		
		if (mapMarkerOptions.size() > 0) {
			
			mMap.clear();
			
			Iterator item = mapMarkerOptions.keySet().iterator();
			
			
			while (item.hasNext()) {
				MarkerOptions mOptions = (MarkerOptions) mapMarkerOptions.get(item.next());
				Marker marker = mMap.addMarker(mOptions);
				if ((mOptions.getPosition().latitude == foundPlaces.get(0).getLatitude()) &&
						mOptions.getPosition().longitude == foundPlaces.get(0).getLongitude()) {
					setDestinationForRouting(marker);
				}
			}
		}

	}
	
	 

	@Override
	public void onMapReady(GoogleMap map) {
		// TODO Auto-generated method stub
		mMap = map;
		mMap.setOnCameraChangeListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMyLocationButtonClickListener(this);
		mMap.setMyLocationEnabled(true);
		mMap.setOnInfoWindowClickListener(this);
		
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(theHillsLatLng, PREFERRED_ZOOM_LEVEL));
		
	
		mTheHillsMarker = mMap.addMarker(new MarkerOptions()
				.position(theHillsLatLng).title("The Hills")
				.snippet("Welcome to The Hills")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
		
		Log.d(TAG, "targetPlaceKeyword: " + targetPlaceKeyword + " targetPlaceType: " + targetPlaceType);
		if ((targetPlaceKeyword!= null)&&(targetPlaceType != -1)) {
			Log.d(TAG, "let me know custome search!");
			createTargetMarkers();
		}

	}
	

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Marker got clicked");
		this.setDestinationForRouting(marker);
		return false;
	}
	
	private void setDestinationForRouting(Marker marker) {
		marker.showInfoWindow();
		destinationLatLng = marker.getPosition();
		
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(destinationLatLng.latitude, destinationLatLng.longitude)) // Sets the center of the map to the des
				.zoom(14) // Sets the zoom
				.tilt(30) // Sets the tilt of the camera to 30 degrees
				.build(); // Creates a CameraPosition from the builder
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}
	
	private void setDestinationForRouting(MarkerOptions mOptions) {

		Marker marker = mMap.addMarker(mOptions);
		setDestinationForRouting(marker);
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		Log.d(TAG, "My location button got clicked");

		return false;
	}


	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
//		Log.d(TAG, marker.getSnippet());
		
		new AlertDialog.Builder(this)
		.setTitle(marker.getTitle())
		.setCancelable(true)
		.setItems(new CharSequence[] {"More info","Go to here", "Voice Navigation to here"}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case 0:
					Log.d(TAG, "0 button got clicked");
					break;
				case 1:
			        Routing routing = new Routing(Routing.TravelMode.DRIVING);
			        routing.registerListener(LocationActivity.this);
			        
			        Location currentLocation = ((Application)getApplication()).getCurrentLocation();
					LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			        routing.execute(currentLatLng, destinationLatLng);					
			        break;
				case 2:
					
					break;
				default:
					break;
				}
			}
		})
		.create()
		.show();
	}


	@Override
	public void onRoutingFailure() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onRoutingStart() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
		// TODO Auto-generated method stub

		mMap.clear();
		PolylineOptions polyoptions = new PolylineOptions();
		polyoptions.color(Color.BLUE);
		polyoptions.width(10);
		polyoptions.addAll(mPolyOptions.getPoints());
		mMap.addPolyline(polyoptions);

		// Start marker
		MarkerOptions options = new MarkerOptions();
		
        Location currentLocation = ((Application)getApplication()).getCurrentLocation();
		LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		options.position(currentLatLng);
		options.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.start_blue));
		mMap.addMarker(options);

		// End marker
		options = new MarkerOptions();
		options.position(destinationLatLng);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
		mMap.addMarker(options);
		
		instructions = new ArrayList<String>();
		
		Log.d(TAG, "Number of the segment " + route.getNumOfSeg());
		
		int i = 1;
		for (Segment aSegment : route.getSegments()) {
			Log.d(TAG, aSegment.getInstruction());
			instructions.add("Step " + i + " " + aSegment.getInstruction());
			i++;
		}
		
		Log.d(TAG, "the size of instructions: " + instructions.size());
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, instructions){

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View view = super.getView(position, convertView, parent);
		        TextView text = (TextView) view.findViewById(android.R.id.text1);
		        text.setTextColor(Color.BLACK);
		        return view;
		    }			
		};
		listInstructions.setAdapter(adapter);
		
		viewSwitcher.showNext();
	}
	
	public void onBackToPlaceBtn(View view) {
		viewSwitcher.showPrevious();
		
		if (mTTS.isSpeaking()) {
			mTTS.stop();
		}
	}
	
	public void onVoiceInstructionBtn(View view) {
		this.readCurrentInstructions();
	}
	
	private void readCurrentInstructions() {
		if ((instructions != null)&& readyToSay) {
			Log.d(TAG, "I am ready to read it!");
			for (String speech : instructions) {
				mTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
			}
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		this.readyToSay = true;
//		mTTS.setSpeechRate(0.8f);
		mTTS.speak(firstToSay, TextToSpeech.QUEUE_FLUSH, null);
	}
}
