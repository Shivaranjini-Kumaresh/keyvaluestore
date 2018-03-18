package com.sidd.cache.core;

/**
 * Created by Siddharth on 3/17/18.
 */
public interface ICache<K,V> {

    public void put(K key, V val);

    public Object get(K key);

}
