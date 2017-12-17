import java.util.ArrayList;

public class Client{

    private String pseudonyme;//chaque client doit avoir un pseudonyme
    private String ip;
    private int port;

    public Client() {
    }

    public Client(String pseudonyme, String ip,int port) {
        this.pseudonyme = pseudonyme;
        this.ip = ip;
        this.port = port;
    }

    public String getPseudonyme() {
        return pseudonyme;
    }

    public void setPseudonyme(String pseudonyme) {
        this.pseudonyme = pseudonyme;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
