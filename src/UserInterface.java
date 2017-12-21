import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//http://blog.csdn.net/baolong47/article/details/6735853
//广播地址如果是255*4  自己也会收到自己的
//问题已经解决  判断收到的和自己的  不一样再加入列表

public class UserInterface {

    private static final String BOARDCAST_IP = "230.0.0.1";
    private static final int BOARDCAST_PORT = 3000;

    private String HOSTIP;

    MulticastSocket boardSocket;//接收广播消息
    InetAddress boardAdresse;//广播地址
    DatagramSocket sender;//数据流套接字 相当于码头 用于发送信息

    Client client = new Client();

    private JFrame frame;
    private JTextField textField ;
    private JTextArea textArea;
    private JTextArea pseudonyme = new JTextArea();
    private JButton connect = new JButton("Connecter");
    private JButton leave = new JButton("Quitter");
    private JButton send = new JButton("Envoyer");
    private JTextArea port = new JTextArea();
    private JList userList;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightScroll;
    private JScrollPane leftScroll;
    private JSplitPane centerSplit;

    private boolean isConnected = false;

    private ArrayList<Client> actifs = new ArrayList<>();

    public UserInterface(){

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setForeground(Color.BLUE);
        textField=new JTextField();

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1,7));
        northPanel.add(new JLabel("pseudonyme"));
        northPanel.add(pseudonyme);
        northPanel.add(new JLabel("       port"));
        northPanel.add(port);

        northPanel.add(connect);
        northPanel.add(leave);
        northPanel.setBorder(new TitledBorder("Info"));

        userList = new JList(tranforme(actifs));

        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(new TitledBorder("Messages"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder("Users Actifs"));

        southPanel = new JPanel(new BorderLayout());
        southPanel.add(textField,"Center");
        southPanel.add(send,"East");
        southPanel.setBorder(new TitledBorder("Write"));


        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(100);

        frame = new JFrame("P2P chatting");
        frame.setLayout(new BorderLayout());
        frame.add(northPanel,"North");
        frame.add(centerSplit,"Center");
        frame.add(southPanel,"South");
        frame.setSize(600,400);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width-frame.getWidth())/2,(screen_height-frame.getHeight())/2);
        frame.setVisible(true);

        try {
           String[] rec = new String[2];
           rec=InetAddress.getLocalHost().toString().split("/");
           HOSTIP =rec[1] ;
        }catch (Exception e){
           System.out.println("******获取本地主机失败*******");
        }

       //Jlist点击事件  双击
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //双击用户名字 可以与其聊天
                if(e.getClickCount()==2){
                   String name = (String)userList.getSelectedValue();
                   Client toenvoyer = new Client();
                   //首先要通过用户名字查表  找到其对应的IP地址  然后建立TCP连接
                   for(int i = 0;i < actifs.size();i++){
                       if(name.equals(actifs.get(i).getPseudonyme())){
                             toenvoyer = actifs.get(i);
                       }
                   }
                   //接下来要弹出一个窗口并建立TCP连接  开启TCP监听线程
                    TCPPanel tcpPanel = new TCPPanel(client,toenvoyer);

                }
            }
        });

        //commencer connecter
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

             String pseu = pseudonyme.getText().trim();
             String po=port.getText().trim();
             int port = Integer.parseInt(po);

             if(pseu!=null&&HOSTIP!=null&&pseu!=""){

                 client.setPseudonyme(pseu);
                 client.setIp(HOSTIP);
                 client.setPort(port);

                 System.out.println("***test***"+client.getPseudonyme()+client.getIp()+client.getPort());

                 initSocket();
                 boolean con = connecter(pseu,HOSTIP,port);
                if(!con){
                    System.out.println("*****加入连接失败******");
                }else{
                    isConnected=true;
                }

             }
            }
        });

        //离开连接
        leave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!isConnected){
                    JOptionPane.showMessageDialog(frame,"Vous avez deja quitte","Error",JOptionPane.ERROR_MESSAGE);
                }
                try{
                    boolean flag = leave();
                    if(flag==false){
                        throw new Exception("断开异常");
                    }
                    JOptionPane.showMessageDialog(frame,"成功断开");
                }catch (Exception exc){

                }
            }
        });

        //点击send发送
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                send();
            }
        });
        //回车发送
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                send();
            }
        });

        //关闭窗口时强制断开连接  然后退出程序
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(isConnected){
                  //向全网发送通告离开后再离开
                    boolean resultat = leave();

                    if(!resultat){
                        System.out.println("******强制离线失败*****");
                    }
                }
                System.exit(0);
            }
        });
    }

    public static void main(String[] args){

        new UserInterface();

    }

    public void send(){
        if(!isConnected){
            JOptionPane.showMessageDialog(frame,"Vous n'etre pas encore connecter!","error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = textField.getText().trim();
        if(message==null || message.equals("")){
            JOptionPane.showMessageDialog(frame,"Vous ne pouvez pas envoyer rien","error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        sendMessage(message);
        textField.setText(null);
    }

    public void initSocket(){
        try{
            boardSocket = new MulticastSocket(BOARDCAST_PORT);
            boardAdresse = InetAddress.getByName(BOARDCAST_IP);
            boardSocket.joinGroup(boardAdresse);
            boardSocket.setLoopbackMode(false);
            sender = new DatagramSocket();
        }catch(Exception e){
            System.out.println("******连接初始化失败*****");
        }

    }
    //向全网通告  加入连接
    public boolean connecter(String pseudonyme , String ip,int port){

        //判断此次连接是否成功 用于返回
        boolean statu = false;

        byte[] b = new byte[1024];
        DatagramPacket packet;

        try{
            //一旦开始连接  就要一直监听信息
            new Thread(new UDPThread()).start();
           // System.out.println("******已经开始监听*****");

            b=("connect/"+pseudonyme+"/"+ip+"/"+client.getPort()).getBytes();
            packet=new DatagramPacket(b,b.length,boardAdresse,BOARDCAST_PORT);
            sender.send(packet);
            statu = true;
            isConnected = true;

        }catch (Exception e){
            System.out.println("******加入广播失败*****");
            statu = false;

        }
        //在成功连接后 要开始监听TCP端口消息

        if(statu==true){
            new Thread(new TCPThread()).start();
        }
        return statu;
    }

    //将消息发送至全部用户
    public void sendMessage(String message){

        byte[] b = new byte[1024];
        DatagramPacket packet;

        //将自己的名字和消息一起广播出去
        b=("Message/"+client.getPseudonyme()+"/"+message).getBytes();
        packet=new DatagramPacket(b,b.length,boardAdresse,BOARDCAST_PORT);
        try{
            sender.send(packet);
        }catch (Exception e){
            System.out.println("*****发送广播消息失败*****");
        }

    }

    //离开连接  需要向全网发送消息通告
    public boolean leave(){

        byte[] b = new byte[1024];
        DatagramPacket packet;
        try{
            b=("leave/"+client.getPseudonyme()).getBytes();
            packet=new DatagramPacket(b,b.length,boardAdresse,BOARDCAST_PORT);
            sender.send(packet);
            isConnected = false;

        }catch (Exception e){
            System.out.println("******离线异常*****");
            return false;
        }


        return true;
    }


    //将一个Client的Array List转换成String[] 为了jlist显示
    public String[] tranforme(ArrayList<Client> e){

        String[] resultat = new String[e.size()];

        for(int i = 0 ; i < e.size();i++){
            resultat[i]=e.get(i).getPseudonyme();
        }
        return resultat;
    }

    class UDPThread implements Runnable {

        @Override
        public void run() {
            DatagramPacket inPacket;
            String[] rec = new String[4];
                 while(true){
                     try{
                                    inPacket = new DatagramPacket(new byte[1024],1024);
                                    boardSocket.receive(inPacket);
                                    String info =new String(inPacket.getData(),0,inPacket.getLength());
                                    rec = info.split("/");

                                    System.out.println(info);

                                    //新连接主机
                                    if(rec[0].equals("connect")){
                                        //收到连接请求  需要返回是否可以连接  若可以 将对方加入自己的激活列表
                                        //先判断发这个消息的是不是自己
                                        if(rec[1].equals(client.getPseudonyme())&&rec[2].equals(client.getIp())&&client.getPort()==Integer.parseInt(rec[3])){

                                        }else{

                                            boolean repeatPseu=false;
                                            for(int i=0;i<actifs.size();i++){
                                                if(rec[1].equals(client.getPseudonyme())){
                                                    repeatPseu=true;
                                                }
                                            }
                                            if(!repeatPseu){
                                                //没人重复域名 将其加入自己列表并返回OK和自己信息
                                                Client newclient = new Client(rec[1],rec[2],Integer.parseInt(rec[3]));
                                                actifs.add(newclient);

                                                userList.setListData(tranforme(actifs));

                                                byte[] re = new byte[1024];
                                                DatagramPacket packet;
                                                try{
                                                    re=("OK/"+client.getPseudonyme()+"/"+client.getIp()+"/"+client.getPort()).getBytes();
                                                    InetAddress ret=InetAddress.getByName(rec[2]);
                                                    packet=new DatagramPacket(re,re.length,InetAddress.getByName(BOARDCAST_IP),/*RECEIVE_BOARDCAST_PORT*/BOARDCAST_PORT);
                                                    sender.send(packet);

                                                }catch (Exception e){
                                                    System.out.println("******返回消息异常*****");
                                                }

                                            }else{

                                                byte[] re = new byte[1024];
                                                DatagramPacket packet;
                                                try{
                                                    re=("repeat/").getBytes();
                                                    InetAddress ret=InetAddress.getByName(rec[2]);
                                                    packet=new DatagramPacket(re,re.length,ret,BOARDCAST_PORT);
                                                    sender.send(packet);

                                                }catch (Exception e){
                                                    System.out.println("******返回消息异常*****");
                                                }

                                            }

                                        }

                    }//有主机离开
                    else if(rec[0].equals("leave")){
                        //删掉用户列表中离线用户
                           for(int i=0;i<actifs.size();i++) {
                               if (rec[1].equals(actifs.get(i).getPseudonyme())) {
                                   actifs.remove(i);
                               }
                           }
                           userList.setListData(tranforme(actifs));
                                    }
                    //收到回复可以 即已经加入连接 需要将返回的信息加入自己的列表中
                    else if(rec[0].equals("OK")){
                        boolean toadd = true;
                        if(rec[1].equals(client.getPseudonyme())&&rec[2].equals(client.getIp())&&client.getPort()==Integer.parseInt(rec[3])){
                                toadd = false;
                         }else{
                               for(int i=0;i<actifs.size();i++) {
                                     if(actifs.get(i).getPseudonyme().equals(rec[1])){
                                         toadd = false;
                                     }else{

                                     }
                                         }
                               }

                        if(toadd){
                            Client c = new Client(rec[1],rec[2],Integer.parseInt(rec[3]));
                            actifs.add(c);
                            userList.setListData(tranforme(actifs));
                        }else{

                        }


                    }//自己命名与别人重复 需要更改名字
                    else if(rec[0].equals("Repeat")){

                    }
                    //收到广播消息  就要显示在自己面板上
                    else if(rec[0].equals("Message")){
                    textArea.append(rec[1]+" a dit : "+rec[2]+"\n");

                                    }

                }catch (Exception e){
                    System.out.println("*****线程出错******");
                }
            }

        }
    }

    //循环监听TCP连接请求
    /***********************************************************
     * 你要写的代码在这里  这是一个tcp监听线程
     * 需要做的就是连接以后监听有没有别人发来tcp连接请求  若有 请开启一个TCPPanel
     * 注意 TCPPanel 构造函数需要传入两个Client对象 一个是你自己 一个是和你通信的人
     *
     *****************************************************************/
    class TCPThread implements Runnable{

        @Override
        public void run() {

            while (true){




            }
        }
    }
}

