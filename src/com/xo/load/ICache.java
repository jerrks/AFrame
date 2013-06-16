package com.xo.load;

import java.util.Iterator;

/**
 * Interface for cache utility.
 *
 * @param <K> the type of keys
 * @param <V> the type of cached values
 */
public interface ICache<K, V> {

    /**
     * get cached data of given key, return null if cache missed.
     */
    public V get(K key);

    /**
     * cache data for given key. Null key or data will be ignored.
     */
    public void put(K key, V data);

    /**
     * Just keep a soft reference
     */
    public void putWeak(K key, V data);

    /**
     * clear cache.
     */
    public void clear();

    /**
     * release all strong references to cached data.
     */
    public void release();
    
    /**
     * get all the keys
     * */
    public Iterator<K> keys();
    
    /**
     * 
     * */
    public boolean isEmpty();
}
