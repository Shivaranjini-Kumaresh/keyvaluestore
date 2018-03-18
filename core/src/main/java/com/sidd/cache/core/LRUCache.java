package com.sidd.cache.core;

/**
 * Created by Siddharth on 3/17/18.
 */
public class LRUCache extends AbstractCache {

    public LRUCache(int cacheSize) {
        super(cacheSize);
    }

    public void put(Object key, Object val)
    {
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
        boolean res = list.remove(key);
        if (res)
        {
            list.addFirst(key);
            return map.get(key);
        }
        return null;
    }
}
