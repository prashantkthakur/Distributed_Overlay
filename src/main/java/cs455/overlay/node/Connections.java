package cs455.overlay.node;

import cs455.overlay.transport.TCPSender;

import java.net.Socket;
import java.net.UnknownHostException;

public class Connections {
    private String hostname;
    private String ipAddress;
    private int clientPort;
    private TCPSender tcpSender;
    private Thread thread;
    private Socket socket;
    private Integer serverPort; // Remote messaging node server port.


    public Connections(Socket insocket, Thread thread, TCPSender tcpSender ) {
        this.socket = insocket;
//        System.out.println("CONNECTION::: socket" +insocket.getPort() + "host "+insocket.getInetAddress().getHostName());
        this.thread = thread;
        this.tcpSender = tcpSender;
        this.hostname = insocket.getInetAddress().getHostName(); // Give remote connection hostname not local
        this.ipAddress = new String(insocket.getInetAddress().getHostAddress()); // Remote messaging node address
        this.clientPort = insocket.getPort();

    }

    public String getHostname(){
        return this.hostname;
    }

    void setServerPort(int serverPort){this.serverPort = serverPort;}

    public Integer getServerPort(){return this.serverPort;}

    int getClientPort(){
        return this.clientPort;
    }

    public String getIpAddress(){
        return this.ipAddress;
    }

    public TCPSender getTcpSender(){
        return this.tcpSender;
    }

    public Thread getThread(){
        return this.thread;
    }

    public Socket getSocket(){
        return this.socket;
    }

//    public Connections getConnection(){
//        return this;
//    }
}
