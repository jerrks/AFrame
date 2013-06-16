package com.xo.app;

import java.util.ArrayList;
import java.util.List;

import com.xo.load.Extra;
import com.xo.load.ImageLoader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;


/**
 * Base class for activity using {@link #ImageLoadAdapter}. Derived class must call
 * method {@link #addLifeCycleListener} in its onCreate() method.
 */
public class LoadingActivity extends Activity {

    /**
     * Interface that monitor activity life-cycle events.
     */
    public interface LifeCycleListener {
        public void onResume();

        public void onPause();

        public void onStop();

        public void onDestroy();
    }

    protected boolean mStatusActive = false;
    protected boolean mStatusDestroyed = false;
    protected List<LifeCycleListener> mMonitors;

    public void addLifeCycleListener(LifeCycleListener listener) {
    	if(listener == null) return;
        if (mMonitors == null) {
            mMonitors = new ArrayList<LifeCycleListener>();
        }
        mMonitors.add(listener);
    }

    @Override
    protected void onResume() {
        if (mMonitors != null) {
            for (LifeCycleListener monitor : mMonitors) {
                monitor.onResume();
            }
        }
        mStatusDestroyed = false;
        mStatusActive = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mMonitors != null) {
            for (LifeCycleListener monitor : mMonitors) {
                monitor.onPause();
            }
        }
        mStatusActive = false;
        mStatusDestroyed = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mMonitors != null) {
            for (LifeCycleListener monitor : mMonitors) {
                monitor.onStop();
            }
        }
        mStatusDestroyed = false;
        mStatusActive = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
    	mStatusActive = false;
    	mStatusDestroyed = true;
        if (mMonitors != null) {
            for (LifeCycleListener monitor : mMonitors) {
            	if(monitor != null)
            		monitor.onDestroy();
            }
        }
        super.onDestroy();
    }
    
    private ImageLoader<Extra> mLoader;
    
    protected void bindImage(String key,String url,ImageView image,Extra extra){
    	if(mLoader == null){
    		throw new NullPointerException("image loader must be initiatized | call initImageLoader frist.");
    	}else{
    		mLoader.bindImage(key, url, image, extra);
    	}
    }
    
    protected void onImageLoad(String key, String url, ImageView image, Extra extra,
    		Bitmap bm){}
    
    protected void initImageLoader(String cacheDir,int loadRes){
    	if(mLoader != null) return;
    	mLoader = new ImageLoader<Extra>(cacheDir, true,loadRes,loadRes){
    		@Override
    		protected void onImageLoaded(String key, String url,
    				ImageView image, Extra extra, Bitmap bm) {
    			super.onImageLoaded(key, url, image, extra, bm);
    			onImageLoad(key, url, image, extra, bm);
    		}
    	};
    	addLifeCycleListener(mLoader);
    }
}
