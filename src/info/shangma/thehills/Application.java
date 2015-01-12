package info.shangma.thehills;

import info.shangma.thehills.map.outside.Place;
import info.shangma.thehills.voice.util.ConnectToServerThread;
import info.shangma.thehills.voice.util.ServerThread;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Application extends android.app.Application {
	
	private static final String TAG = "Application";
	// bluetooth
	public final static String UUID = "3606f360-e4df-11e0-9572-0800200c9a66";
	
    public BluetoothAdapter bluetoothAdapter;
    public BroadcastReceiver discoverDevicesReceiver;
    public BroadcastReceiver discoveryFinishedReceiver;
    
    //---store all the discovered devices---
    public ArrayList<BluetoothDevice> discoveredDevices;
    public ArrayList<String> discoveredDevicesNames;
    
    //---store all the paired devices---
    public ArrayList<BluetoothDevice> pairedDevices; 
    
    //---thread for running the server socket---
    public ServerThread serverThread;

    //---thread for connecting to the client socket---
    public ConnectToServerThread connectToServerThread;
    
    
    //---device location info for the whole application
	private LocationManager locationManager;
	private Location currentLocation;
	
	private ArrayList<Place> foundPlaces;

	public ArrayList<Place> getFoundPlaces() {
		return foundPlaces;
	}

	public void setFoundPlaces(ArrayList<Place> foundPlaces) {
		this.foundPlaces = foundPlaces;
	}

	public Application() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		// location
		this.currentLocation();
			
		// bluetooth
		discoveredDevices = new ArrayList<BluetoothDevice>();
        discoveredDevicesNames = new ArrayList<String>();
        pairedDevices = new ArrayList<BluetoothDevice>();
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();
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
			/*
			CameraPosition cameraPosition = new CameraPosition.Builder()
										.target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())) 
										.zoom(14) // Sets the zoom
										.tilt(30) // Sets the tilt of the camera to 30 degrees
										.build(); // Creates a CameraPosition from the builder
			
			mMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			*/
			
			Log.e(TAG, "current location is : " + location);
		}
		
		/*
		currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		
		mCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
		.position(currentLatLng)
		.title("Your Location")
		.snippet("This is your current location")
		.icon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360)));
		*/

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
	
	public Location getCurrentLocation() {
		return currentLocation;
	}
	
	// bluetooth capability
	/*
	
	public void makeDiscoverable() {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); 
        startActivity(i);
	}
	
	*/
	
    //---discover other bluetooth devices---
    public void DiscoverDevices()    
    {
        //---query for all paired devices---
        //QueryPairedDevices();           

        //---discover other devices---
        DiscoveringDevices();        
    }
    
	//---utility funcation used to discover other bluetooth devices---
    private void DiscoveringDevices() {  	
    	if (discoverDevicesReceiver == null) {
            discoverDevicesReceiver = new BroadcastReceiver() {            	
            	//---fired when a new device is discovered---
                @Override
                public void onReceive(Context context, Intent intent) {                
                    String action = intent.getAction();

                    //---a device is discovered---
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        //---get the BluetoothDevice object from 
                        // the Intent---
                        BluetoothDevice device = 
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE);

                        //---add the name and address to an array 
                        // adapter to show in a ListView---
                        //---only add if the device is not already 
                        // in the list---
                        if (!discoveredDevices.contains(device)) {
                            //---add the device---
                            discoveredDevices.add(device);
                            
                            //---add the name of the device; used for 
                            // ListView---
                            discoveredDevicesNames.add(device.getName());

                            //---display the items in the ListView---
                            // no display for our app
                        }                    
                    }
                }
            };
    	}
    	
    	if (discoveryFinishedReceiver==null) {
    		discoveryFinishedReceiver = new BroadcastReceiver() {
    			//---fired when the discovery is done---
    			@Override
				public void onReceive(Context context, Intent intent) {
	
			        Toast.makeText(getBaseContext(), 
			        		"Discovery completed.", 
			        		Toast.LENGTH_LONG).show();
			        unregisterReceiver(discoveryFinishedReceiver);
				}    			
    		};
    	}
    	
        //---register the broadcast receivers---
        IntentFilter filter1 = new
            IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter2 = new
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            
        registerReceiver(discoverDevicesReceiver, filter1);
        registerReceiver(discoveryFinishedReceiver, filter2);
        
        Toast.makeText(getBaseContext(), 
        		"Discovery in progress...please wait...", 
        		Toast.LENGTH_LONG).show();
        bluetoothAdapter.startDiscovery();
    }
    
	// for bluetooth notification
	
    public void SendMessage(String sendingString) 
    {    
        if (this.connectToServerThread!=null) {
            ///=========
            //connectToServerThread.commsThread.write(
            //		txtMessage.getText().toString());
            
            new WriteTask().execute(sendingString);
            ///=========

        } else {
            Toast.makeText(this, "Sending error", 
                Toast.LENGTH_SHORT).show();
        }
    }
	
    private class WriteTask extends AsyncTask<String, Void, Void> {
		protected Void doInBackground(String... args) {
			try {
				Application.this.connectToServerThread.commsThread.write(args[0]);
	        } catch (Exception e) {
	        	Log.d(TAG, e.getLocalizedMessage());
	        }
			return null;
		}
	}
}
