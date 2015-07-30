package com.android.soundcommunicate;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class PowerSupply {
	private boolean powerIsSupplying = false;
	
	private AudioManager audioManager;
	private int curretIndex=0;
	private int maxIndex=0;
	public PowerSupply(Context context,AudioManager audioManager){
		
			//audioManager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		this.audioManager=audioManager;
	}
	
	
	public PowerSupply(Context context) {
		// TODO Auto-generated constructor stub
		this.audioManager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}


	public static int pwMinBufferSize = AudioTrack.getMinBufferSize(EncoderCore.getPowerSupplySamplerate(),
																	AudioFormat.CHANNEL_OUT_STEREO,
																	AudioFormat.ENCODING_PCM_8BIT);
	AudioTrack pwAT;
	
	public boolean powerIsSupplying(){
		return powerIsSupplying;
	}

	public void pwStart(byte[] carrierSignal) {
		if(powerIsSupplying)
			pwStop();
		//��������
		curretIndex=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxIndex=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxIndex,AudioManager.FLAG_SHOW_UI);
		
		pwAT = new AudioTrack(AudioManager.STREAM_MUSIC,
				EncoderCore.getPowerSupplySamplerate(),
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_8BIT, 
				pwMinBufferSize*2,
				AudioTrack.MODE_STATIC);

		powerIsSupplying = true;   
		pwAT.write(carrierSignal, 0, EncoderCore.getPowerSupplyBufferSize());
		pwAT.flush();
		pwAT.setStereoVolume(1, 0);
		pwAT.setLoopPoints(0, EncoderCore.getPowerSupplyBufferSize(), -1);  
		pwAT.play();
	}

	public void pwStop() {
		if(pwAT != null)
		{	
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,curretIndex,AudioManager.FLAG_SHOW_UI);
			pwAT.release();
			pwAT = null;
		}
		powerIsSupplying = false;
	}
}
//endclass
