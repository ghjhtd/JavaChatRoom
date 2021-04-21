package MainChat;

import MainChat.ChatMessage;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;


/**
 * 客户端类，继承JFrame,实现监听接口
 */
public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;  //序列号
    private JLabel label;  //昵称输入标签
    private JLabel label1; //私聊对象名字输入标签
    private JTextField tf; //昵称输入文本域
    private JTextField tf1;//私聊对象昵称文本域

    private JTextField tfServer, tfPort;
    //连接，断开，群发，查看在线人数按钮
    private JButton login, logout, submit,onlineb;
    //私聊，查看群文件，上传文件，下载文件按钮
    private JButton privateChat,fileList,putFile,downFile;

    private JTextArea ta;
    private JTextField tb; //信息输入框
    private JLabel online_num;

    private boolean connected;

    private Client client;
    private int defaultPort;
    private String defaultHost;

    /**
     * @param args
     * 主方法，生成客户端
     */
    public static void main(String[] args) {
        try
        {
            //设置windows组件外观
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //设置图像界面外观为当前系统
            //设置图形界面外观为跨平台外观
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //设置图形界面统一字体
            InitGlobalFont(new Font("", Font.ROMAN_BASELINE, 13));

        }catch(Exception e) {
            System.out.println("组件外观设置错误！");
        }
        //构造客户端GUI
        new ClientGUI("81.70.90.194", 2333);

    }

    ClientGUI(String host, int port) {
        //设置窗口名字
        super("基于C/S架构的Swing&Socket简易聊天室");
        //端口号
        defaultPort = port;
        //ip地址
        defaultHost = host;

        // 顶部的Server地址和端口面板
        JPanel northPanel = new JPanel(new GridLayout(2,1,1,5));
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 4, 3));
        JPanel title = new JPanel(new GridLayout(1,1, 0, 2));

        //服务器地址
        tfServer = new JTextField(host);
        //服务器端口
        tfPort = new JTextField("" + port);
        //设置标签水平对齐方式为右对齐
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        //添加组件到面板里
        serverAndPort.add(new JLabel("Server地址:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("端口地址:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));

        northPanel.add(serverAndPort);
        title.add(new JLabel("<html><body><div style='color:#4b5154;font-size:11px'>信管1901 龚皓靖&赵蒙恩:</div></body></html>", SwingConstants.CENTER));
        //num_person.add(online_num);
        northPanel.add(title);
        add(northPanel, BorderLayout.NORTH);

        // 中间的分割面板
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        ta = new JTextArea("", 80, 50);
        ta.setEditable(false);
        centerPanel.add(new JScrollPane(ta));
        add(centerPanel, BorderLayout.CENTER);

        // 昵称和登陆控制面板
        JPanel namepanel=new JPanel(new GridLayout(1,7, 7, 7));
        JPanel namepane2=new JPanel(new GridLayout(1,7, 7, 7));
        label = new JLabel("<html><body><div style='color:#ca1d12'>请输入你的昵称:</div></body></html>", SwingConstants.CENTER);
        label1 = new JLabel("<html><body><div style='color:#ca1d12'>请输入私聊对象昵称:</div></body></html>", SwingConstants.CENTER);
        label.setForeground(Color.BLUE);
        label1.setForeground(Color.RED);
        //设置默认昵称
        double d = Math.random();
        final int i = (int)(d*100);
        tf = new JTextField("匿名者"+i);
        tf1 = new JTextField("私聊对象名字");
        //设置昵称背景
        tf.setBackground(Color.blue);
        tf1.setBackground(Color.blue);
        //添加昵称到面板
        namepanel.add(label);
        namepanel.add(tf);
        namepanel.add(new JLabel(""));
        namepane2.add(label1);
        namepane2.add(tf1);
        namepane2.add(new JLabel(""));

        //添加按钮和监听事件
        submit = new JButton("<html><body><div style='color:#14a43b'>发送</div></body></html>");
        submit.addActionListener(this);
        onlineb= new JButton("<html><body><div style='color:#1285c3'>在线人员</div></body></html>");
        onlineb.addActionListener(this);
        login = new JButton("<html><body><div style='color:#1282c3'>连接</div></body></html>");
        login.addActionListener(this);
        logout = new JButton("<html><body><div style='color:#e22318'>断开</div></body></html>");
        logout.addActionListener(this);

        privateChat = new JButton("<html><body><div style='color:#14a43b'>私聊</div></body></html>");
        privateChat.addActionListener(this);
        fileList= new JButton("<html><body><div style='color:#1285c3'>文件列表</div></body></html>");
        fileList.addActionListener(this);
        putFile = new JButton("<html><body><div style='color:#1282c3'>上传文件</div></body></html>");
        putFile.addActionListener(this);
        downFile = new JButton("<html><body><div style='color:#e22318'>下载文件</div></body></html>");
        downFile.addActionListener(this);


        //禁用一些按钮在没登录之前
        onlineb.setEnabled(false);
        submit.setEnabled(false);
        logout.setEnabled(false);
        privateChat.setEnabled(false);
        fileList.setEnabled(false);
        putFile.setEnabled(false);
        downFile.setEnabled(false);

        //将按钮添加到面板
        namepanel.add(submit);
        namepanel.add(onlineb);
        namepanel.add(login);
        namepanel.add(logout);

        namepane2.add(privateChat);
        namepane2.add(fileList);
        namepane2.add(putFile);
        namepane2.add(downFile);
        //信息输入框
        tb = new JTextField("请先输入昵称连接后聊天...");
        tb.setEditable(false);

        //底部面板
        JPanel southPanel =new JPanel(new GridLayout(3,1, 5, 3));
        southPanel.add(tb);
        southPanel.add(namepanel);
        southPanel.add(namepane2);
        add(southPanel, BorderLayout.SOUTH);

        //窗口设置
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 720);
        setVisible(true);
        setLocationRelativeTo(null);
        tf.requestFocus();


    }

    // 信息展示框展示消息
    void append(String str) {
        ta.append(str);
        //设置光标停留位置
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    void append2(int o_num){
        online_num.setText(Integer.toString(o_num));
    }

    //连接失败组件变化函数
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        submit.setEnabled(false);
        onlineb.setEnabled(false);

        //whoIsIn.setEnabled(false);
        label.setText("请输入昵称:");
        tf.setText("");
        tb.setEditable(false);

        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        tfServer.setEditable(true);
        tfPort.setEditable(true);

        tf.removeActionListener(this);
        connected = false;
    }


    /**
     * @param e
     * 事件监听函数
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        // 点击到登出的事件
        if(o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            tfServer.setEditable(true);
            tfPort.setEditable(true);
            tb.setEditable(false);
            tf.setEditable(true);
            submit.setEnabled(false);
            onlineb.setEnabled(false);
            tb.removeActionListener(this);
            tb.setText("请先输入昵称连接后聊天...");
            return;
        }
        // 查看在线人员事件
        if(o == onlineb) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            return;
        }
        // 点击群发的事件
        if(o == submit || o == tb) {
            String tbt = tb.getText().trim();
            if(tbt.length()==0){
                ta.append("消息不能为空!\n");
                return;
            }
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tb.getText()));
            tb.setText("");
            return;
        }

        // 点击查询文件列表的事件
        if(o == fileList) {
            client.sendMessage(new ChatMessage(ChatMessage.FILELIST, tb.getText()));
            return;
        }
        // 点击上传文件的事件
        if(o == putFile) {
            client.sendMessage(new ChatMessage(ChatMessage.PUTFILE, tb.getText().trim()));
            String server = tfServer.getText().trim();
            if(server.length() == 0){
                ta.append("Server地址不能为空!\n");
                return;
            }
            if(tb.getText().trim().length() == 0){
                ta.append("请在输入框输入文件名和路径\n");
                return;
            }

            try {
                File file = new File(tb.getText());
                PutFile put = new PutFile(server,tb.getText().trim(),this,true);
                put.sendFile();
            } catch (IOException e1) {
                this.append("文件名或文件路径出错\n");
                ta.append("请输入正确的文件名和路径\n");
            }
            return;
        }
        // 点击下载文件的事件
        if(o == downFile) {
            client.sendMessage(new ChatMessage(ChatMessage.DOWNFILE, tb.getText().trim()));
            if(tb.getText().trim().length()==0){
                ta.append("文件名不能为空!\n");
                tb.setText("请输入要下载的正确的文件名\n");
                return;
            }
            try {
                PutFile put1 = new PutFile(tfServer.getText().trim(),tb.getText().trim(),this,false);
                put1.sendFile();
            } catch (IOException e1) {
                ta.append("请求文件出错\n");
            }
            return;
            }


        //点击私聊的事件
        if(o == privateChat) {
            String tbt = tb.getText().trim();
            String privateName = tf1.getText().trim();
            if(tbt.length()==0){
                ta.append("消息不能为空!\n");
                return;
            }
            if(privateName.length()==0){
                ta.append("私聊对象名字不能为空!\n");
                return;
            }
            client.sendMessage(new ChatMessage(ChatMessage.PRIVATECHAT, tb.getText(),tf1.getText()));
            tb.setText("");
            return;
        }

        //连接按钮事件触发
        if(o == login) {
            String username = tf.getText().trim();
            if(username.length() == 0){
                ta.append("昵称不能为空!\n");
                return;
            }
            String server = tfServer.getText().trim();
            if(server.length() == 0){
                ta.append("Server地址不能为空!\n");
                return;
            }
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0){
                ta.append("端口不能为空!\n");
                return;
            }
            //端口号判断
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                ta.append("端口号不合法!\n");
                return;
            }

            // 创建GUI客户端连接
            client = new Client(server, port, username, this);
            //是否启动成功
            if(!client.start())
                return;

            tf.setText(username);
            tf.setEditable(false);
            label.setText("您的聊天昵称：");
            tb.setEditable(true);
            tb.setText("输入消息...");
            connected = true;

            // 禁用连接按钮
            login.setEnabled(false);
            // 启用3按钮
            submit.setEnabled(true);
            logout.setEnabled(true);
            onlineb.setEnabled(true);
            privateChat.setEnabled(true);
            fileList.setEnabled(true);
            putFile.setEnabled(true);
            downFile.setEnabled(true);

            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // 监听信息发送事件
            tb.addActionListener(this);
        }

    }



    /**
     * @param font
     * 设置全局字体
     */
    private static void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}

