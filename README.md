# Key-Value-Store
This is a simple key value store server.It maintains a mapping, in memory, of keys to values.The server allows a maximum of N key-values to be in memory at any one time, as further entries are added, the oldest entries are evicted first(It follows LRU as its eviction strategy). The cache size is configurable.
This store can either be used an in-proc java library or can also be started as a stand alone TCP server. There is a Java based client library that can be used to interact with the server remotely.
This framework has got following components:
* LRU Cache 
* TCP Server exposing the remote service. The server is developed using Java NIO
* A java based client for interacting with the TCP server

# Operations exposed
* PUT key value
* GET key
    
# Network protocol
Following messages are supported:
* PUT\nKEY\nVALUE\n - Returns OK\n incase of success and FAIL\n incase of failure
* GET\nKEY\n - Returns <VALUE>\n incase of data exists and "" incase of no data
  

# Starting the Server
java com.sidd.cache.server.CacheServer  location Of server.properties file
Example : java com.sidd.cache.server.CacheServer /Users/siddharth/Downloads/server.properties

The server starts at the given port as specified in server.properties file with the configured cache size

# Using the Java client for interacting with server
   public class CacheServerTest 
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
