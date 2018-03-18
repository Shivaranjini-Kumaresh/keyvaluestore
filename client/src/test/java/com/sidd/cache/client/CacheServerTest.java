package com.sidd.cache.client;

import java.io.IOException;

/**
 * Created by Siddharth on 3/18/18.
 */
public class CacheServerTest {

    public static void main(String[] args) throws IOException {
        Cache cache = new Cache("127.0.0.1", 8888);
        for(int c = 1; c <=20; c++)
        {
            cache.put(""+c, ""+c);
            System.out.println(cache.get(""+c));
        }

        for(int c = 1; c <=5; c++)
        {
            System.out.println(cache.get(""+c));
        }
    }
}
