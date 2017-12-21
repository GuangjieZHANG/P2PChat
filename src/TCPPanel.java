import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
            public void actionPerformed(ActionEvent e) {
                String message = jTextField.getText().trim();
                if(message.equals("")||message.equals(" ")){
                    JOptionPane.showMessageDialog(jFrame,"Vous ne pouvez pas envoyer rien","error",
                            JOptionPane.ERROR_MESSAGE);
                }

                /***********************
                 * 你要做的就是将message用TCP发送给另一个Client to ， 你是oringin
                 * 端口号和IP地址都在里面  可以直接用
                 **************************/

                //接下来要开始tcp连接

            }
        });


    }

  /*  public static void main(String[] args){
        new TCPPanel();
    }*/

}
