package com.android.soundcommunicate;

public class EncoderCore {

	private final String TAG = "CordCore";
	
//	private final int msgBuffSize = MessageOut.msgMinBufferSize;

	private final int pwBuffSize = PowerSupply.pwMinBufferSize;

	static{
   
		System.loadLibrary("encoder_decoder_core");   

	}
	
//	public int getMsgBuffSize(){
//		return msgBuffSize;
//	}
	
	public int getPowerBuffSize(){
		return pwBuffSize;   
	}

//	public native static int getMsgSamplerate();
	
	public native static int getPowerSupplySamplerate();
	
	public native static int getEncoderBufferSize();
	
	public native static int getPowerSupplyBufferSize();

	public native byte[] carrierSignalGen(int freq);

//	public native byte[] soundCording(byte[] msg);
}