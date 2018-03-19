package com.sidd.cache.server;

import com.sidd.cache.core.CacheFactory;
import com.sidd.cache.core.ICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Properties;

/**
 * Created by Siddharth on 3/17/18.
 */
public class CacheServer implements Runnable{

    //TODO Read from a properties file
    public final static String ADDRESS = "127.0.0.1";
    public static int PORT = -1;
    private int cacheSize = -1;

    public final static long TIMEOUT = 10000;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Map<SocketChannel,byte[]> dataTracking;

    ICache<String, String> cache;

    private static final Logger logger = LoggerFactory.getLogger(CacheServer.class);

    private CacheServer(String propertiesFilePath)
    {
        try
        {
            logger.info("properties file path: " + propertiesFilePath);
            File f = new File(propertiesFilePath);
            Properties pros = new Properties();
            pros.load(new FileInputStream(f));
            logger.info("properties file loaded from : " + f.getAbsolutePath());
            PORT = Integer.parseInt(pros.get("port").toString());
            cacheSize = Integer.parseInt(pros.get("cachesize").toString());
            dataTracking = new HashMap<SocketChannel, byte[]>();

            init();

            //Initialize
            cache = CacheFactory.newInstance(cacheSize);
            logger.info("cache size " + cacheSize);
        }catch(Exception e)
        {
            logger.error("Error loading server.properties. " + e);
        }
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
            logger.info(ADDRESS+":"+PORT + " started");

        }catch(Exception e)
        {
            logger.error("initialization failed", e);
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
                        logger.debug("Connection accepted");
                    }
                    if (key.isWritable()){
                        logger.debug("Writing to socket");
                        write(key);
                    }
                    if (key.isReadable()){
                        logger.debug("Reading from socket");
                        read(key);
                    }
                }

            }
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            closeConnection();
        }
    }
    private void accept(SelectionKey key) throws IOException
    {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
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
            logger.error("Reading problem, closing connection", e);
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
        logger.info("process(). Message received: " + dataStr.trim());
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
            logger.info("PUT " + key + "," + val + ", Result: " + result);
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
            logger.info("GET " + key + ", Result: " + result);
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
        logger.info("Shutting down");
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
        String filePath = "";
        if(args.length == 1)
        {
            filePath = args[0];
        }
        else
        {
            filePath = "server.properties";
        }

        Thread server = new Thread(new CacheServer(filePath));
        server.start();
    }
}
