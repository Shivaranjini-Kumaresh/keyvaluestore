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
* java com.sidd.cache.server.CacheServer  <location Of server.properties file>
* Example : java com.sidd.cache.server.CacheServer /Users/siddharth/Downloads/server.properties
* The server starts at the given port as specified in server.properties file with the configured cache size
   
   ## Server.log output:
   
   2018-03-19_12:36:00.079 [main] INFO  com.sidd.cache.server.CacheServer - properties file path: server.properties
   2018-03-19_12:36:00.082 [main] INFO  com.sidd.cache.server.CacheServer - properties file loaded from : /Users/sr250345/Documents/siddharth/personal/mygithub/keyvaluestore/server.properties
  2018-03-19_12:36:00.105 [main] INFO  com.sidd.cache.server.CacheServer - 127.0.0.1:8888 started
  2018-03-19_12:36:00.107 [main] INFO  com.sidd.cache.server.CacheServer - cache size 5000


# Using the Java client for interacting with server

   * Cache cache = new Cache("127.0.0.1", 8888);
   * cache.put("1", "one");
   * System.out.println(cache.get("1"));
