package info.shangma.thehills.map.outside;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author Shang Ma
 *
 * www.shangma.info
 */

public class PlacesService {
	
	private final static String TAG = "PlacesService";
	
	public final static int TYPE_PLACE = 0;
	public final static int KEYWORD_PLACE = 1;

	private String API_KEY;

	public PlacesService(String apikey) {
		this.API_KEY = apikey;
	}

	public void setApiKey(String apikey) {
		this.API_KEY = apikey;
	}

	public ArrayList<Place> findPlaces(double latitude, double longitude,
			String placeSpacification, int type) {

		String urlString = makeUrl(latitude, longitude, placeSpacification, type);
		Log.e(TAG, urlString);

		try {
			String json = getJSON(urlString);

			System.out.println(json);
			JSONObject object = new JSONObject(json);
			JSONArray array = object.getJSONArray("results");

			ArrayList<Place> arrayList = new ArrayList<Place>();
			for (int i = 0; i < array.length(); i++) {
				try {
					Place place = Place
							.jsonToPontoReferencia((JSONObject) array.get(i));
					Log.v("Places Services ", "" + place);
					arrayList.add(place);
				} catch (Exception e) {
				}
			}
			return arrayList;
		} catch (JSONException ex) {
			Logger.getLogger(PlacesService.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return null;
	}

	// https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey
	private String makeUrl(double latitude, double longitude, String place, int type) {
		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/search/json?");
		if (type == this.TYPE_PLACE) {
			if (place.equals("")) {
				urlString.append("&location=");
				urlString.append(Double.toString(latitude));
				urlString.append(",");
				urlString.append(Double.toString(longitude));
				urlString.append("&radius=2000");
				// urlString.append("&types="+place);
				urlString.append("&key=" + API_KEY);
			} else {
				urlString.append("&location=");
				urlString.append(Double.toString(latitude));
				urlString.append(",");
				urlString.append(Double.toString(longitude));
				urlString.append("&radius=2000");
				urlString.append("&types=" + place);
				urlString.append("&key=" + API_KEY);
			}
		} else if (type == this.KEYWORD_PLACE) {
			if (place.equals("")) {
				Log.e(TAG, "cannot be empty");
			} else {
				urlString.append("&location=");
				urlString.append(Double.toString(latitude));
				urlString.append(",");
				urlString.append(Double.toString(longitude));
//				urlString.append("&radius=2000");
				urlString.append("&rankby=distance");
				urlString.append("&keyword="+place);
				urlString.append("&key=" + API_KEY);
			}
		}
		
		return urlString.toString();
	}

	protected String getJSON(String url) {
		return getUrlContents(url);
	}

	private String getUrlContents(String theUrl) {
		StringBuilder content = new StringBuilder();

		try {
			URL url = new URL(theUrl);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream()), 8);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line + "\n");
			}
			bufferedReader.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}
}