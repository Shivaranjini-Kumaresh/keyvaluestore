package com.sidd.cache.core;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Siddharth on 3/17/18.
 */
public abstract class AbstractCache<K,V> implements ICache<K,V> {
    int cacheSize;
    HashMap<K, V> map;
    LinkedList<K> list;

    public AbstractCache(int cacheSize)
    {
        this.cacheSize = cacheSize;
        map = new HashMap<K, V>(cacheSize);
        list = new LinkedList<K>();
    }
    public  abstract void put(K key, V val);

    public  abstract V get(K key);

    public void invalidate(K key)
    {
        list.remove(key);
        map.remove(key);
    }
    public void prune() //Removes the tail
    {
        K key = list.removeLast();
        map.remove(key);
    }
}
