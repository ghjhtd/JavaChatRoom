package MainChat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 消息类，实现序列化接口，可序列化
 */
public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;
    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, PRIVATECHAT = 3, FILELIST =4,
            PUTFILE = 5,DOWNFILE = 6;
    private int type;
    private String message;
    private String privateName;

    ChatMessage(int type, String message ) {
        this.type = type;
        this.message = message;
    }

    ChatMessage(int type, String message ,String privateName) {
        this.type = type;
        this.message = message;
        this.privateName = privateName;
    }


    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
    String getPrivateName() {
        return privateName;
    }
}

//文件上传下载线程,用来处理文件的上传和下载

//服务器文件接收发送线程
class ServerFile extends Thread {

    private static final int SERVER_PORT = 6666; // 服务端端口

    private ServerSocket server;
    private String path;
    private Server.ClientThread clientThread;

    public ServerFile(String path, Server.ClientThread clientThread) throws Exception {
        server = new ServerSocket(SERVER_PORT);
        this.path = path;
        this.clientThread = clientThread;
    }

    /**
     * 使用线程处理每个客户端传输的文件
     *
     * @throws Exception
     */
    @Override
    public void run(){
        while (true) {
            System.out.println("-----------等待连接-------- ");
            Socket socket = null;//接收连接服务端的客户端对象
            try {
                socket = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("ip" + socket.getInetAddress() + "已连接");
            new Thread(new Transfer(socket,path,clientThread), "thread1").start();// 每接收到一个Socket就建立一个新的线程来处理它
            System.out.println(Thread.currentThread().getName());
        }
    }
}



    /**
     * 处理客户端传输过来的文件线程类
     */
class Transfer implements Runnable {

        private Socket socket;
        private DataInputStream dis;
        private FileOutputStream fos;
        private FileInputStream fis;
        private DataOutputStream dos;
        private String path;
        private Server.ClientThread clientThread;

        public Transfer(Socket socket,String path,Server.ClientThread clientThread) {
            this.socket = socket;
            this.path = path;
            this.clientThread = clientThread;
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());

                // 文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                boolean state = dis.readBoolean(); //接收boolean数值，为true则接收数据，为false则发送数据
                if(state){
                    //接收客户端数据
                    File directory = new File(path);
                    if(!directory.exists()) {
                        directory.mkdir();
                    }
                    File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                    System.out.println("file"+file);
                    fos = new FileOutputStream(file);

                    // 开始接收文件
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                        fos.write(bytes, 0, length);
                        fos.flush();
                    }
                    System.out.println("======== 文件接收成功 [File Name：" + fileName + "] ");
                }
                else {
                    //发送给客户端数据
                    try {
                        File file = new File(System.getProperty("user.dir")+"\\file" + File.separatorChar + fileName);
                        fis = new FileInputStream(file);
                        dos = new DataOutputStream(socket.getOutputStream());//返回此套接字的输出流
                        //文件名、大小等属性
                        dos.writeUTF(file.getName());
                        dos.flush();
                        dos.writeLong(file.length());
                        dos.flush();
                        // 开始传输文件
                        System.out.println("======== 开始发送"+fileName+"文件 ========");
                        byte[] bytes = new byte[1024];
                        int length = 0;

                        while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                            dos.write(bytes, 0, length);
                            dos.flush();
                        }
                        clientThread.writeMsg(fileName+"文件下载成功\n");
                        System.out.println("======== "+fileName+"文件发送成功 ========");

                    }
                    catch (Exception e){
                        System.out.println(fileName+"文件发送失败");
                        clientThread.writeMsg(fileName+"文件下载失败\n");
                    }


            }} catch (Exception ee) {
                ee.printStackTrace();
            } finally {
                try {
                    if(fos != null){
                        fos.close();}
                    if(dis != null){
                        dis.close();}

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


//上传文件
class PutFile extends Socket{
    private  String ip; //ip地址
    private final int SERVER_PORT=6666;  //文件服务器端口
    private Socket client;
    private FileInputStream fis;
    private DataOutputStream dos;
    private FileOutputStream fos;
    private DataInputStream dis;
    private ClientGUI gui;
    private String path;
    private boolean state;//上传or请求资源 状态

    //创建客户端，并指定接收的服务端IP和端口号
    public PutFile(String ip,String path,ClientGUI gui,boolean state) throws IOException{
        this.client=new Socket(ip,SERVER_PORT);
        this.gui = gui;
        this.path = path;
        this.state = state;
        gui.append("成功连接文件服务器\n");
    }

    //向服务端传输文件
    public void sendFile() throws IOException {
        try {
            if(state){
                try {
                    File file = new File(path);
                    fis = new FileInputStream(file);
                    //BufferedInputStream bi=new BufferedInputStream(new InputStreamReader(new FileInputStream(file),"GBK"));
                    dos = new DataOutputStream(client.getOutputStream());//client.getOutputStream()返回此套接字的输出流
                    //文件名、大小等属性
                    dos.writeUTF(file.getName());
                    dos.flush();
                    dos.writeLong(file.length());
                    dos.flush();
                    dos.writeBoolean(state);
                    dos.flush();
                    // 开始传输文件
                    gui.append("======== 开始传输文件 ========\n");
                    byte[] bytes = new byte[1024];
                    int length = 0;

                    while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        dos.write(bytes, 0, length);
                        dos.flush();
                    }
                    gui.append("======== 文件传输成功 ========\n");
                }
                catch (Exception ee){gui.append("文件名或路径出错\n");}
                finally {
                    try {
                        if (fis != null) fis.close();
                    } catch (Exception e) {
                    }
                    try {
                        if (dos != null) dos.close();
                    } catch (Exception e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (Exception e) {
                    }
                    try {
                        if (dis != null) dis.close();
                    } catch (Exception e) {
                    }
                }
            }
            else {
                dos = new DataOutputStream(client.getOutputStream());//client.getOutputStream()返回此套接字的输出流
                //文件名、大小等属性
                dos.writeUTF(path);
                dos.flush();
                dos.writeLong(path.length());
                dos.flush();
                dos.writeBoolean(state);
                dos.flush();
                dis = new DataInputStream(client.getInputStream());
                // 文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                File directory = new File(System.getProperty("user.dir")+"\\file" );
                if(!directory.exists()) {
                    directory.mkdir();
                }
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                gui.append("file"+file);
                fos = new FileOutputStream(file);

                // 开始接收文件
                byte[] bytes = new byte[1024];
                int length = 0;
                while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                }
                gui.append("======== 文件接收成功 [File Name：" + fileName + "] ");
            }

        }catch(IOException e){
            e.printStackTrace();
            gui.append("客户端文件传输异常\n");
        }finally{
            try {
                if (fis != null) fis.close();
            } catch (Exception e) {
            }
            try {
                if (dos != null) dos.close();
            } catch (Exception e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (Exception e) {
            }
            try {
                if (dis != null) dis.close();
            } catch (Exception e) {
            }
        }
    }
}

