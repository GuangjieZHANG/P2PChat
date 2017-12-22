import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class TCPPanel {

    public JFrame jFrame;
    public JTextArea jTextArea;
    public JTextField jTextField;
    public JButton send;
    public JPanel southPanel;
    public JScrollPane scrollPane;

    public TCPPanel(Client origine , Client to) {

        //用于展示双方聊天内容
        jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setForeground(Color.BLUE);


        //编辑输入内容
        jTextField = new JTextField();
        send = new JButton("Envoyer");

        scrollPane = new JScrollPane(jTextArea);

        southPanel = new JPanel(new BorderLayout());
        southPanel.add(jTextField,"Center");
        southPanel.add(send,"East");

        jFrame = new JFrame("Chatting");
        jFrame.setLayout(new BorderLayout());
        jFrame.add(scrollPane);
        jFrame.add(southPanel,"South");
        jFrame.setSize(400,400);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        jFrame.setLocation((screen_width-jFrame.getWidth())/2,(screen_height-jFrame.getHeight())/2);
        jFrame.setVisible(true);

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) throws IOException {
                String message = jTextField.getText().trim();
                if(message.equals("")||message.equals(null)){
                    JOptionPane.showMessageDialog(jFrame,"Vous ne pouvez pas envoyer rien","error",
                            JOptionPane.ERROR_MESSAGE);
                }

                /***********************
                 * 你要做的就是将message用TCP发送给另一个Client to ， 你是oringin
                 * 端口号和IP地址都在Client里面  可以直接用
                 **************************/

                    //客户端请求与本机在端口建立TCP连接   
                    Socket clientSock;
                    
						clientSock = new Socket(origine.getIp(), origine.getPort());
						clientSock.setSoTimeout(10000); 
                    //获取键盘输入   
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));  
                    //获取Socket的输出流，用来发送数据到服务端    
                    PrintStream out = new PrintStream(clientSock.getOutputStream());  
                    //获取Socket的输入流，用来接收从服务端发送过来的数据    
                    BufferedReader buf =  new BufferedReader(new InputStreamReader(clientSock.getInputStream()));  
                    boolean flag = true;  
                    while(flag){  
                        System.out.print("Message：");  
                        String str = input.readLine();  
                        //发送数据到服务端    
                        out.println(str);  
                        if("bye".equals(str)){  
                            flag = false;  
                        }else{  
                            try{  
                                //从服务器端接收数据有个时间限制（系统自设，也可以自己设置），超过了这个时间，便会抛出该异常  
                                String echo = buf.readLine();  
                                System.out.println(echo);  
                            }catch(SocketTimeoutException e1){  
                                System.out.println("Time out, No response");  
                            }  
                        }  
                    }  
                    input.close();  
                    if(clientSock != null){  
                        //如果构造函数建立起了连接，则关闭套接字，如果没有建立起连接，自然不用关闭  
                        clientSock.close(); //只关闭socket，其关联的输入输出流也会被关闭  
                     
                } 
                
                

            }
        });


    }
    

    
    

  /*  public static void main(String[] args){
        new TCPPanel();
    }*/

}
