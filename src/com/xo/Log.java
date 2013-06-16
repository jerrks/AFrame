package com.xo;

public class Log {
	
	protected static boolean mEnableDubge = false;
	
	public static void enableDubge(boolean able){
		mEnableDubge = able;
	}
	
	public static void d(String tag, String msg){
		if(mEnableDubge)
			android.util.Log.d(tag, msg);
	}
	
	public static void i(String tag, String msg){
		if(mEnableDubge)
			android.util.Log.i(tag, msg);
	}
	
	public static void v(String tag, String msg){
		if(mEnableDubge)
			android.util.Log.v(tag, msg);
	}
	
	public static void w(String tag, String msg){
		if(mEnableDubge)
			android.util.Log.w(tag, msg);
	}
	
	public static void e(String tag, String msg){
		if(mEnableDubge)
			android.util.Log.e(tag, msg);
	}

}
