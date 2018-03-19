# Key-Value-Store
This is a simple key value store server which exposes API for putting a key value and getting a value based on a key.It follows LRU algorithm for pruning the cache when the size goes beyond the desired cache size. This library can either be used an in-proc library or can also be started as a stand alone TCP server. There is a Java based client library also provided.
This framework has got following components:
* LRU Cache 
* TCP Server exposing the cache service. The server is developed using Java NIO
* A java based client library for interacting with TCP server

# APIs exposed
public interface ICache<K,V> {

    public void put(K key, V val);

    public Object get(K key);
}

# Network protocol
Following messages are supported:
* PUT\nKEY\nVALUE\n - Returns OK\n incase of success and FAIL\n incase of failure
* GET\nKEY\n - Returns <VALUE>\n incase of data exists and "" incase of no data
  

# Starting the Server
java com.sidd.cache.server.CacheServer
The server starts at the given port as specified in server.properties file with the configured cache size

# Using the Java client for interacting with serverpublic class CacheServerTest 
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
