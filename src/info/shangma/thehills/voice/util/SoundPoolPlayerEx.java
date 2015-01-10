package info.shangma.thehills.voice.util;

import info.shangma.thehills.R;
import android.content.Context;
import android.util.Log;
import root.gast.audio.util.SoundPoolPlayer;

public class SoundPoolPlayerEx extends SoundPoolPlayer{
	
	private static final String TAG = "SoundPoolPlayerEx";

	public SoundPoolPlayerEx(Context pContext) {
		super(pContext);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void playSound() {
		// TODO Auto-generated method stub
		Log.d(TAG, "playing sound");
		this.loadShortResource(R.raw.hello);
		this.playShortResource(R.raw.hello);
	}

}
