package info.shangma.thehills.mapoutside;

import info.shangma.thehills.R;

import java.util.ArrayList;
import java.util.Random;


import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

public class LocationActivity extends FragmentActivity implements 
							OnMapReadyCallback, OnCameraChangeListener,
							OnMarkerClickListener, OnMyLocationButtonClickListener,
							OnInfoWindowClickListener,
							RoutingListener {
	
	private final static String TAG = "LocationActivity";
	
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
	private String[] places;
	private LocationManager locationManager;
	private Location currentLocation;
	private LatLng currentLatLng;
	
	private final Random mRandom = new Random();
	private ProgressDialog dialog;
	
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
    
    // for routing
//    private Location destinationLocation;
    private LatLng destinationLatLng;
   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		setContentView(R.layout.activity_location);
		
		places = getResources().getStringArray(R.array.places);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		
		checkBoxAll = (CheckBox) findViewById(R.id.checkbox_all);
	    checkBoxATM = (CheckBox) findViewById(R.id.checkbox_atm);
	    checkBoxBank = (CheckBox) findViewById(R.id.checkbox_bank);
	    checkBoxPharmacy = (CheckBox) findViewById(R.id.checkbox_pharmacy);
	    checkBoxCafe = (CheckBox) findViewById(R.id.checkbox_cafe);
	    checkBoxRestaurant = (CheckBox) findViewById(R.id.checkbox_restaurant);
	    checkBoxShopping = (CheckBox) findViewById(R.id.checkbox_shopping);
	
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}


	private void currentLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		String provider = locationManager
				.getBestProvider(new Criteria(), false);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			locationManager.requestLocationUpdates(provider, 0, 0, listener);
		} else {
			currentLocation = location;
			Log.d(TAG, "places[0]: " + places[0]);
//			new GetPlaces(MainActivity.this, places[0].toLowerCase().replace("-", "_")).execute();
			
			CameraPosition cameraPosition = new CameraPosition.Builder()
										.target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())) 
										.zoom(14) // Sets the zoom
										.tilt(30) // Sets the tilt of the camera to 30 degrees
										.build(); // Creates a CameraPosition from the builder
			
			mMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			Log.e(TAG, "location : " + location);
		}
		
		currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		mCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
		.position(currentLatLng)
		.title("Your Location")
		.snippet("This is your current location")
		.icon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360)));

	}

	private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

		private Context context;
		private String places;
		private int locationType;

		public GetPlaces(Context context, String places, int type) {
			this.context = context;
			this.places = places;
			this.locationType = type;
		}

		@Override
		protected void onPostExecute(ArrayList<Place> result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					mMap.addMarker(new MarkerOptions()
							.title(result.get(i).getName())
							.position(
									new LatLng(result.get(i).getLatitude(), result
											.get(i).getLongitude()))
							.icon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360))
							.alpha((float)(mRandom.nextFloat()*0.5+0.5)) // alpha is always greater than 0.5
							.snippet(result.get(i).getVicinity()));
				}
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(result.get(0).getLatitude(), result
								.get(0).getLongitude()))
						.zoom(14) 
						.tilt(30) 
						.build(); 
				
				mMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading..");
			dialog.isIndeterminate();
			dialog.show();
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... args) {
			PlacesService service = new PlacesService(
					"AIzaSyCIjhxW0q69NqchGywwFOtl0ERpQQjTgTE");
			Log.d(TAG, "location type is: " + this.locationType);
			ArrayList<Place> findPlaces = service.findPlaces(currentLocation.getLatitude(), // 28.632808
					currentLocation.getLongitude(), places, this.locationType); // 77.218276

			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
				Log.e(TAG, "places : " + placeDetail.getName());
			}
			return findPlaces;
		}
	}
	
	public void onCheckboxAll(View view) {
		this.displayTargetPlace();
	}
	public void onCheckboxATM(View view) {
		checkBoxAll.setChecked(false);
		this.displayTargetPlace();
	}

	public void onCheckboxBank(View view) {
		checkBoxAll.setChecked(false);
		this.displayTargetPlace();
	}

	public void onCheckboxPharmacy(View view) {
		checkBoxAll.setChecked(false);

		this.displayTargetPlace();
	}

	public void onCheckboxcafe(View view) {
		checkBoxAll.setChecked(false);

		this.displayTargetPlace();
	}

	public void onCheckboxRestaurant(View view) {
		checkBoxAll.setChecked(false);

		this.displayTargetPlace();
	}

	public void onCheckboxShopping(View view) {
		checkBoxAll.setChecked(false);

		this.displayTargetPlace();
	}
	
	private void displayTargetPlace() {
		
		if (checkBoxAll.isChecked()) {
			new GetPlaces(LocationActivity.this, "atm|bank|pharmacy|cafe|restaurant|shopping_mall", 0).execute();
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
			new GetPlaces(LocationActivity.this, targetString, 0).execute();
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
		
//		mMyLocationMarker = mMap.addMarker(new MarkerOptions()
//				.position(testLatLng)
//				.title("Shang")
//				.snippet("Shang's Home")
//				.icon(BitmapDescriptorFactory
//						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		
		mTheHillsMarker = mMap.addMarker(new MarkerOptions()
				.position(theHillsLatLng).title("The Hills")
				.snippet("Welcome to The Hills")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));

		this.currentLocation();

	}
	

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Marker got clicked");
		destinationLatLng = marker.getPosition();
		return false;
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
		.setItems(new CharSequence[] {"More info","Go to here"}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case 0:
					Log.d(TAG, "0 button got clicked");
					break;
				case 1:
			        Routing routing = new Routing(Routing.TravelMode.WALKING);
			        routing.registerListener(LocationActivity.this);
			        
					currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			        routing.execute(currentLatLng, destinationLatLng);					
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
		options.position(currentLatLng);
		options.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.start_blue));
		mMap.addMarker(options);

		// End marker
		options = new MarkerOptions();
		options.position(destinationLatLng);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
		mMap.addMarker(options);
	}
	
	private LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e(TAG, "location update : " + location);
			currentLocation = location;
			locationManager.removeUpdates(listener);
		}
	};
}
