package MainChat;

import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {

    private ObjectInputStream sInput;       // to read from the socket
    private ObjectOutputStream sOutput;     // to write on the socket
    private Socket socket;
    private ClientGUI cg;
    private String server, username;
    private int port;

    Client(String server, int port, String username, ClientGUI cg) {
        //服务器ip地址
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }

    /**
     * @return
     * 开始连接
     */
    public boolean start() {
        // 连接server
        try {
            socket = new Socket(server, port);
        }
        // 失败
        catch(Exception ec) {
            display("连接到Server异常:" + ec);
            return false;
        }

        String msg = "连接到 " + socket.getInetAddress() + ":" + socket.getPort()+"成功";
        display(msg);

        /* new 两个反序列化数据流 */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("创建I/O流异常: " + eIO);
            return false;
        }

        // 创建线程监听服务器信息
        new ListenFromServer().start();
        // 发送一个username的String信息，
        // 其他信息以ChatMessage对象形式发送
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("连接时发生错误: " + eIO);
            disconnect();
            return false;
        }
        // 通知GUI连接成功
        return true;
    }


    //展示信息到GUI里
    private void display(String msg) {
        cg.append(msg + "\n");
    }

    /*
     * 发送信息到Server
     */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("向服务器发送信息异常" + e);
        }
    }

    /*
     *
     * 断开连接和I/O流
     */
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}
        if(cg != null)
            cg.connectionFailed();
    }


    /**
     * 等待服务端消息并发送到GUI展示
     * 新建一个线程
     */
    class ListenFromServer extends Thread {

        @Override
        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    //int o_num=sInput.read();
                    //cg.append2(o_num);
                    cg.append(msg);
                }
                catch(IOException e) {
                    display("您已经与服务器断开了连接。 ");
                    cg.connectionFailed();
                    break;
                }
                //虽然传的是String 但必须捕获类异常
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}
