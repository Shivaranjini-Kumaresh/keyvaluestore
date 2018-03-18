package com.sidd.cache.server;

import com.sidd.cache.core.CacheFactory;
import com.sidd.cache.core.ICache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Siddharth on 3/17/18.
 */
public class CacheServer implements Runnable{

    //TODO Read from a properties file
    public final static String ADDRESS = "127.0.0.1";
    public final static int PORT = 8888;
    public final static long TIMEOUT = 10000;
    final int cacheSize = 1000;
    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Map<SocketChannel,byte[]> dataTracking;

    ICache<String, String> cache = CacheFactory.newInstance(5);

    public CacheServer()
    {
        dataTracking = new HashMap<SocketChannel,byte[]>();
        init();

        //Initialize com.sidd.cache
        cache = CacheFactory.newInstance(cacheSize);
    }
    private void init()
    {
        try
        {
            // Open a Selector
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);//Make it non-blocking
            serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("server initialized...");

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void run()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                selector.select(TIMEOUT);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext())
                {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid())
                    {
                        continue;
                    }
                    if (key.isAcceptable())
                    {
                        //Accepting connection
                        accept(key);
                        System.out.println("Connection accepted...");
                    }
                    if (key.isWritable()){
                        //System.out.println("Writing...");
                        write(key);
                    }
                    if (key.isReadable()){
                        //System.out.println("Reading...");
                        read(key);
                    }
                }

            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally{
            closeConnection();
        }
    }
    private void accept(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        /*socketChannel.register(selector, SelectionKey.OP_WRITE);
        String msg = "You are connected to " + ADDRESS + ":" + PORT + "\n";
        byte[] hello = new String(msg).getBytes();
        dataTracking.put(socketChannel, hello);*/

        socketChannel.register(selector, SelectionKey.OP_READ);
    }
    private void read(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();
        int read;
        try
        {
            read = channel.read(readBuffer);
        }
        catch (IOException e)
        {
            System.out.println("Reading problem, closing connection");
            key.cancel();
            channel.close();
            return;
        }
        if (read == -1)
        {
            /*System.out.println("Nothing was there to be read, closing connection");
            channel.close();
            key.cancel();*/
            return;
        }
        readBuffer.flip();
        byte[] data = new byte[1000];
        readBuffer.get(data, 0, read);


        process(key,data);
    }
    private void process(SelectionKey selectionKey, byte[] data)
    {
        String result = null;

        String dataStr = new String(data);
        //PUT KEY VALUE , GET KEY
        String[] tokens = dataStr.split("\n");
        String operation = tokens[0];
        //TODO Put more logic for message validation
        if("PUT".equalsIgnoreCase(operation))
        {
            String key = tokens[1].trim();
            String val = tokens[2].trim();
            cache.put(key, val);
            result = "OK" + "\n";
            System.out.println("PUT " + key + "," + val + ", Result: " + result);
        }
        else if("GET".equalsIgnoreCase(operation))
        {
            String key = tokens[1].trim();
            String val  = (String) cache.get(key);
            if(val != null)
            {
                result = val +  "\n";
            }
            else
            {
                result = "" +  "\n";//Returns an empty string if the key is not found
            }
            System.out.println("GET " + key + ", Result: " + result);
        }
        else
        {
            result = "Invalid command" + "\n";;
        }

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        dataTracking.put(socketChannel, result.getBytes());
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }
    private void write(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();
        byte[] data = dataTracking.get(channel);
        dataTracking.remove(channel);
        channel.write(ByteBuffer.wrap(data));
        key.interestOps(SelectionKey.OP_READ);
    }
    private void closeConnection(){
        System.out.println("Shutting down");
        if (selector != null){
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args)
    {
        Thread server = new Thread(new CacheServer());
        server.start();
    }
}
