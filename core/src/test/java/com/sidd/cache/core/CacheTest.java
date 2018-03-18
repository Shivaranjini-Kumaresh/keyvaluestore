package com.sidd.cache.core;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Siddharth on 3/17/18.
 */

public class CacheTest {

    @Test
    public void getPutAndPruneTest()
    {
        ICache<String, String> cache = CacheFactory.newInstance(5);
        cache.put("1", "1");
        cache.put("2", "2");
        cache.put("3", "3");
        cache.put("4", "4");
        cache.put("5", "5");

        Assert.assertTrue("1".equals(cache.get(""+1).toString()));
        Assert.assertTrue("2".equals(cache.get(""+2).toString()));
        Assert.assertTrue("3".equals(cache.get(""+3).toString()));
        Assert.assertTrue("4".equals(cache.get(""+4).toString()));
        Assert.assertTrue("5".equals(cache.get(""+5).toString()));

        cache.put("6", "6");

        Assert.assertEquals(null, cache.get(""+1));
        Assert.assertTrue("6".equals(cache.get(""+6).toString()));

    }

}
