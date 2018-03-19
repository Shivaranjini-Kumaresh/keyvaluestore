package com.sidd.cache.core;

import com.sidd.cache.server.CacheServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Siddharth on 3/17/18.
 */
public class LRUCache extends AbstractCache {

    private static final Logger logger = LoggerFactory.getLogger(LRUCache.class);

    public LRUCache(int cacheSize) {
        super(cacheSize);
    }

    public void put(Object key, Object val)
    {
        logger.info("put() " + "key:" + key + " ,Value:" + val);
        // check if pruning is needed
        if (list.size() == this.cacheSize)
        {
            this.prune();
        }
        list.addFirst(key);
        map.put(key, val);
    }

    public Object get(Object key)
    {
        logger.info("get() " + "key: " + key);
        boolean res = list.remove(key);
        if (res)
        {
            list.addFirst(key);
            return map.get(key);
        }
        return null;
    }
}
