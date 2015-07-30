package com.caihua.mybluetooth;

import android.util.Log;

/*
 * 数值转换工具类
 */
public class ByteUtil {
	public static byte[] charToByte(char c) {
		byte[] b = new byte[2];
		b[0] = (byte) ((c & 0xFF00) >> 8);
		b[1] = (byte) (c & 0xFF);
		return b;
	}
	

	public static void showBuffer(String tag, byte[] bytes,int size) {
		Log.e(tag, toString(bytes, size));
	}
	
	public static char byteToChar(byte[] b) {
		char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
		return c;
	}

	public static String toHex(byte b) {
		return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
	}

	public static String toString(byte[] bytes,int size) {
		String ss="";
		for (int i = 0; i < size; i++) {
			ss+=toHex(bytes[i]);
					//+" ";
		}		
		return ss;
	}
	public static int toInt(String hex) {
		int ss=0;
		if ((hex.charAt(0)-'A')>=0) {
			ss+=(hex.charAt(0)-'A'+10)*16;
		}else {
			ss+=(hex.charAt(0)-'0')*16;
		}
		if ((hex.charAt(1)-'A')>=0) {
			ss+=hex.charAt(1)-'A'+10;
		}else {
			ss+=hex.charAt(1)-'0';
		}
		return ss;  
	}

	public static int[] byte2int(byte[] bytes,int index,int size){
		int[] temp=new int[size*2];
		int k=0;
		for (int i = 0; i < size; i++) {
			temp[k++]=0xF0 & bytes[index+i];
			temp[k++]=0xF0 & (0x0F & bytes[index+i])<<4;
		}
		return temp;
	}
	
	public static int[] str2int(String[] strings){
		int[] temp=new int[strings.length*2];
		int k=0;
		for (int i = 0; i < strings.length; i++) {
			temp[k++]=hex2int(strings[i].charAt(0));
			temp[k++]=hex2int(strings[i].charAt(1));
		}
		return temp;
	}

	public static int hex2int(char c){
		if (c>='0'&& c<='9') {
			return (c-'0')*16;
		}
		if (c>='A'&& c<='F') {
			return (c-'A'+10)*16;
		}
		return 0;
	}
	public static int toInt(byte b){
		return (int) b & 0xFF;
	}
}
