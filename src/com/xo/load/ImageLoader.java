package com.xo.load;

import java.util.Iterator;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.xo.app.LoadingActivity;
import com.xo.utils.BitmapUtils;
import com.xo.utils.NetworkUtils;

/**
 * function : frame work for ascy-loading image from memory, image cache and remote service
 * <p> </p>
 * @author JerrksLover at 2013-04-11
 * @version v1.0.0
 * @param E must be extends {@link ImageLoader.Extra} witch contains the image size 'w x h'
 * */

public class ImageLoader<E extends Extra> implements
	LoadingActivity.LifeCycleListener {

protected ICache<String, Bitmap> mImageCache;
protected AsyncLoader<String, String, ImageView, E, Bitmap> mLoader;
protected boolean mActive;
protected final boolean mLocalAsync;
protected final int mDefaultRes;
protected final int mLoadingRes;
protected AsyncLoader.LoaderProxy<String, String, ImageView, E, Bitmap> mProxy;
	private String mCacheDir = "";
	public static final String CACHE_SUFFIX = ".bat";
	
	/**
	* constructor with default cache and loader capacity.
	* @param cacheDir
	* 			 image data will be cached the cache dir,and must not be null or empty
	* @param async
	*            load local image async or not
	* @param defaultRes
	*            default resource id,set 0 use defualt resId {@link android.R.color.white}
	* @param loadingRes
	*            loading resource id,set 0 use defualt resId {@link android.R.color.white}
	*/
	public ImageLoader(String cacheDir,
						  boolean async, 
						      int defaultRes, 
						      int loadingRes) {
		mCacheDir = cacheDir;
		mLocalAsync = async;
		if(defaultRes < 1)
			mDefaultRes = android.R.color.white;
		else
			mDefaultRes = defaultRes;
		if(loadingRes < 1)
			mLoadingRes =  android.R.color.white;
		else
			mLoadingRes = loadingRes;
		init();
		mImageCache = new CacheStrategy<String, Bitmap>();
		mLoader = new AsyncLoader<String, String, ImageView, E, Bitmap>(mProxy);
	}
		
	private void init() {
		mProxy = new AsyncLoader.LoaderProxy<String, String, ImageView, E, Bitmap>() {
			@Override
			public Bitmap doInBackground(String key, String url, ImageView image,
					E extra) {
				Bitmap bm = null;
				if(mLocalAsync) bm = loadImageLocal(key, url, extra);
				if(bm == null) bm = loadImageRemote(key, url, extra);
				return bm;
			}
			@Override
			public void onLoaded(String key, String url, ImageView image, E extra,
					Bitmap bm) {
				if(bm == null)bindImage(key, url, image, extra);
				else onImageLoaded(key, url, image, extra, bm);
			}
		};
	}
	
	/**
	* load image from local in main thread
	*/
	protected Bitmap loadImageLocal(String key, String url, E extra){
		Bitmap bm = null;
		try {
			String name = (!TextUtils.isEmpty(mCacheDir) ? mCacheDir + "/":"" ) + key + CACHE_SUFFIX;
			bm = BitmapUtils.getBitmapFromSD(name);
		}catch (OutOfMemoryError e) {
			System.gc();
		}catch (Exception e) {
		}
		return bm;
	}
	
	/**
	* load image from remote in background thread
	*/
	protected Bitmap loadImageRemote(String key, String url, E extra){
		Bitmap bm = null;
		try {
			final byte[] data = NetworkUtils.downLoadImage(url);
			Thread.sleep(2);
			if(extra == null)
				bm = BitmapUtils.decodeBitmap(data, 0, 0);
			else
				bm = BitmapUtils.decodeBitmap(data, extra.width, extra.heigth);
			if (bm != null){
				String name = key + CACHE_SUFFIX;
				BitmapUtils.saveImageData(data, (!TextUtils.isEmpty(mCacheDir) ? mCacheDir + "/":"" ), name);
			}
		}catch (OutOfMemoryError e) {
			System.gc();
		}catch (Exception e) {
		} 
		return bm;
	}
	
	/**
	* call on main thread when image loaded.
	*/
	protected void onImageLoaded(String key, String url, ImageView image, E extra,
		Bitmap bm) {
		Object objTag = image.getTag(android.R.string.yes);
		boolean matched = (objTag != null && key.equals(objTag));
		
		if (bm == null){
			if(matched)	image.setImageResource(mDefaultRes);
		}else{
			if(matched)	image.setImageBitmap(bm);
			if(mActive) mImageCache.put(key, bm);
			else mImageCache.putWeak(key, bm);
		}
	}
	
	/**
	* bind image
	*/
	public void bindImage(String key, String url, ImageView image, E extra) {
		image.setTag(android.R.string.yes,key);
		if (key == null) {
			image.setImageResource(mDefaultRes);
			return;
		}
		
		Bitmap bm = null;
		if(mImageCache != null && !mImageCache.isEmpty()) bm = mImageCache.get(key); // get from image cache saved in memory
		
		if(bm != null){
			onImageLoaded(key, url, image, extra, bm);
		}else{
			if(url != null && url.length() > 0) { // url is valid then to load image
				image.setImageResource(mLoadingRes);
				mLoader.loadData(key, url, image, extra);
			}else{
				image.setImageResource(mDefaultRes);
			}
		}
	}
	
	/**
	* owner activty should call this in its onDestroy() method to clear cache
	* and stop loader.
	*/
	@Override
	public void onDestroy() {
		try {
			Iterator<String> items = mImageCache.keys();
			if(items != null){
				while(items.hasNext()){
					Bitmap b = mImageCache.get(items.next());
					if(b != null && !b.isRecycled()){
						b.recycle();
						b = null;
					}
				}
			}
			mLoader.stop();
			mImageCache.clear();
		} catch (RuntimeException e) {
		} catch (Exception e) {
		}
		System.gc();
	}
	
	/**
	* owner activity should call this method in its onStop() method to pause
	* loader and reduce cache usage.
	*/
	@Override
	public void onStop() {
		mActive = false;
		mImageCache.release();
		mLoader.pause();
	}
	
	@Override
	public void onPause() {
		// do nothing, handle cache in onStop()
	}
	
	/**
	* owner activity should call this method in its onResume() method to resume
	* loader.
	*/
	@Override
	public void onResume() {
		mActive = true;
		mLoader.resume();
	}
	
	/**
	* this method should be called if whole list changed
	*/
	public void resetLoader() {
		mLoader.invalidate();
	}
}
