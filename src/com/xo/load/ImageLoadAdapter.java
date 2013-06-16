package com.xo.load;

import java.util.ArrayList;
import java.util.List;

import com.xo.app.LoadingActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import android.widget.ImageView;


/**
 * @author JerrksLover at 2013-2-5<p></p>
 * function: as adapter for {@link ListView}, {@link Gallery} and so on
 * to automatic download image and show images asynchronous.
 * @version 1.0.0
 * 
 * @param T item view data,the data for call method {@link getItem}
 * @param E extra data must be extends {@link Extra} witch contains the image size 'w x h'
 * */
public abstract class ImageLoadAdapter<T,E extends Extra> extends BaseAdapter 
	implements LoadingActivity.LifeCycleListener {

	protected ICache<String, Bitmap> mImageCache;
	protected ImageLoader<E> mImageLoader;

	private ArrayList<T> mList;
	private Context mContext;
	
	/**
	 * @param context context for activity
	 * @param async   asyc to local image cache data
	 * @param defaultImage  default image
	 * @param loadingImage  loading image when picture is loading
	 * */
	public ImageLoadAdapter(Context context,
			String cacheDir,
			boolean async, 
			int defaultRes, 
			int loadingRes) {
		mImageLoader = new ImageLoader<E>(cacheDir, async, defaultRes, loadingRes){
			protected void onImageLoaded(String key, String url, ImageView image, E extra, Bitmap bm) {
				super.onImageLoaded(key, url, image, extra, bm);
				ImageLoadAdapter.this.onImageLoaded(key, url, image, extra, bm);
			};
		};
		mContext = context;
		mList = new ArrayList<T>();
	}
	
	public void setDataSet(ArrayList<T> list){
		if(list == null)
			return;
		else{
			if(!mList.isEmpty())
				mList.clear();
			mList.addAll(list);
		}
	}
	
	
	public void updateDataSet(List<T> list){
		if(list == null)
			return;
		else{
			if(!mList.isEmpty())
				mList.clear();
			mList.addAll(list);
		}
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public T getItem(int index) {
		return mList.isEmpty() || index>=getCount() 
			? null : mList.get(index);
	}

	public ArrayList<T> getDataSet(){
		return mList;
	}
	
	@Override
	public long getItemId(int id) {
		return id;
	}

	public Context getContext() {
		return mContext;
	}
	
	public int getItemMaxImageWidth(){
		return 0;
	}
	public int getItemMaxImageHeigth(){
		return 0;
	}
	
	/* ----------------------------------------------------------
	 *   for asyc loading image
	 * ----------------------------------------------------------*/
	/**
	 * call on main thread when image loaded.
	 */
	protected void onImageLoaded(String key, String url, ImageView image, E extra,
			Bitmap bm) {}
	
	/**
	 * bind image
	 */
	protected void bindImage(String key, String url, ImageView image, E extra) {
		mImageLoader.bindImage(key, url, image, extra);
	}


	@Override
	public void notifyDataSetChanged() {
		mImageLoader.resetLoader();
		super.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		mImageLoader.onResume();
	}

	@Override
	public void onPause() {
		mImageLoader.onPause();
	}

	@Override
	public void onStop() {
		mImageLoader.onStop();
	}

	@Override
	public void onDestroy() {
		mImageLoader.onDestroy();
	}
}
