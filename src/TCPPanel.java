import javax.swing.*;
import java.awt.*;

public class TCPPanel {

    public JFrame jFrame;
    public JTextArea jTextArea;
    public JTextField jTextField;
    public JButton send;
    public JPanel northPanel;
    public JPanel southPanel;
    public JScrollPane scrollPane;

    public TCPPanel() {

        //用于展示双方聊天内容
        jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setForeground(Color.BLUE);


        //编辑输入内容
        jTextField = new JTextField();

        send = new JButton("Envoyer");

        northPanel = new JPanel();
        scrollPane = new JScrollPane(jTextArea);
        northPanel.add(scrollPane);

        southPanel = new JPanel(new BorderLayout());
        southPanel.add(jTextField,"Center");
        southPanel.add(send,"East");

        jFrame = new JFrame("Chatting");
        jFrame.setLayout(new BorderLayout());
        jFrame.add(northPanel,"North");
        jFrame.add(southPanel,"South");
        jFrame.setSize(400,400);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        jFrame.setLocation((screen_width-jFrame.getWidth())/2,(screen_height-jFrame.getHeight())/2);
        jFrame.setVisible(true);

    }

    public static void main(String[] args){
        new TCPPanel();
    }

}
