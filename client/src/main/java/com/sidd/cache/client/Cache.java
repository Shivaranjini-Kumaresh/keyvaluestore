package com.sidd.cache.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by Siddharth on 3/18/18.
 */
public class Cache {

    String SERVER_IP = "127.0.0.1";
    int SERVER_PORT  = 8888;

    SocketChannel channel;
    static Selector selector;

    public Cache(String ip, int port)
    {
        this.SERVER_IP = ip;
        this.SERVER_PORT = port;

        connect();
    }
    public void put(String k, String v)
    {
        String msg = "PUT" + "\n" + k + "\n" + v + "\n";
        try
        {
            byte[] result = this.sendMsg(msg);
            System.out.println("\t\t\t Return from PUT: " + new String(result).trim());
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
    public String get(String k)
    {
        String retVal = null;
        String msg = "GET" + "\n" + k + "\n";
        try
        {
            byte[] result = this.sendMsg(msg);
            System.out.println("\t\t\t Return from GET: " + new String(result).trim());
            retVal = new String(result).trim();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
    private void connect()
    {
        try
        {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            selector.select(1000);
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext())
            {
                SelectionKey key = keys.next();
                keys.remove();
                if (!key.isValid()) continue;
                if (key.isConnectable())
                {
                    System.out.println("I am connected to the server");
                    connect(key);
                }
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }
    private byte[] sendMsg(String msg) throws Exception
    {
        selector.select(1000);
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext())
        {
            SelectionKey key = keys.next();
            keys.remove();

            if (!key.isValid())
            {
                continue;
            }
            else if (key.isWritable())
            {
                writeToSocket(key, msg);
                break;
            }
            else if (key.isConnectable())
            {
                System.out.println("I am connected to the server");
                connect(key);
            }
        }
        byte[] response = receiveMsg();
        return response;
    }
    private byte[] receiveMsg() throws Exception
    {
        selector.select(1000);
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        byte[] response = null;
        while (keys.hasNext())
        {
            SelectionKey key = keys.next();
            keys.remove();
            if (!key.isValid()) continue;
            if (key.isConnectable())
            {
                System.out.println("I am connected to the server");
                connect(key);
            }
            if (key.isReadable())
            {
                response = readFromSocket(key);
            }
        }
        return response;
    }
    private byte[] readFromSocket (SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();
        int length;
        try
        {
            length = channel.read(readBuffer);
        }
        catch (Exception e)
        {
            System.out.println("Reading problem, closing connection");
            key.cancel();
            channel.close();
            return null;
        }
        if (length == -1)
        {
            System.out.println("Nothing was read from server");
            channel.close();
            key.cancel();
            return null;
        }
        readBuffer.flip();
        byte[] buff = new byte[1024];
        readBuffer.get(buff, 0, length);
        key.interestOps(SelectionKey.OP_WRITE);
        return buff;
    }
    private void writeToSocket(SelectionKey key, String response) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(response.getBytes()));
        // lets get ready to read.
        key.interestOps(SelectionKey.OP_READ);
    }

    public void connect(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()){
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
    }
    private void close()
    {
        try
        {
            selector.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
