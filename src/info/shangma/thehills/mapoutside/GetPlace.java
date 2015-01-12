package info.shangma.thehills.mapoutside;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class GetPlace extends AsyncTask<Void, Void, Void> {
	
	private final static String TAG = "GetPlace";
	
	private String places;
	private int locationType;
	private Location targetLocation;
	
	private ArrayList<Place> foundPlaces;
	private int numberOfPlaces;
	
	private Context mContext;
	private ProgressDialog mDialog;
	

	public GetPlace(Context context, String places, int type, Location requestLocation) {
		this.places = places;
		this.locationType = type;
		this.targetLocation = requestLocation;
		this.foundPlaces = null;
		this.numberOfPlaces = 0;
		this.mContext = context;
		this.mDialog = null;
	}

	@Override
	protected Void doInBackground(Void... args) {
		// TODO Auto-generated method stub
		PlacesService service = new PlacesService(
				"AIzaSyCIjhxW0q69NqchGywwFOtl0ERpQQjTgTE");
		Log.d(TAG, "location type is: " + this.locationType);
		foundPlaces = service.findPlaces(targetLocation.getLatitude(), // 28.632808
				targetLocation.getLongitude(), places, this.locationType); // 77.218276

		if ((numberOfPlaces=foundPlaces.size())>0) {
			for (int i = 0; i < numberOfPlaces; i++) {
				Place placeDetail = foundPlaces.get(i);
				Log.e(TAG, "places : " + placeDetail.getName());
			}
		}
		return null;	
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		Log.d(TAG, "show dialog");
		mDialog = new ProgressDialog(mContext);
//		mDialog.setCancelable(false);
		mDialog.setMessage("Loading..");
//		mDialog.isIndeterminate();
		mDialog.show();
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	public boolean findAnyPlaces() {
		if (numberOfPlaces > 0) {
			return true;
		}
		return false;
	}
	
	public ArrayList<Place> getFoundPlaces() {
		return foundPlaces;
	}
}