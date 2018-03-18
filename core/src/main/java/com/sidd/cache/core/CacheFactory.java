package com.sidd.cache.core;

/**
 * Created by Siddharth on 3/17/18.
 */
public class CacheFactory {

    public static ICache newInstance(int cacheSize)
    {
        return new LRUCache(cacheSize);
    }
    /*public static ICache newInstance(int cacheSize, EvictionStrategy strategy) {
        if (EvictionStrategy.LRU == strategy)
        {
            return new LRUCache(cacheSize);
        }
        else if (EvictionStrategy.MRU == strategy) {
            return new MRUCache(cacheSize);
        }
        else {
            throw new RuntimeException("Invalid EvictionStrategy");
        }
    }*/
}
