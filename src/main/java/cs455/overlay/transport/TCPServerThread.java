package cs455.overlay.transport;

import cs455.overlay.node.Connections;
import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPServerThread implements Runnable {
    private ServerSocket myServerSocket;
    private Integer portNum = 0;
    private boolean runForever;
    private Node node;


    public TCPServerThread(Node node, ServerSocket socket){
        this.node = node;
        this.myServerSocket = socket;

    }


    @Override
    public synchronized void run() {
        runForever = true;

        while (runForever) {
            try {

                //Bind socket
//                System.out.println("Server listening on port: " + myServerSocket.getLocalPort());
                Socket inSocket = myServerSocket.accept();
                // Create handler for each connection.
                TCPReceiverThread reqHandler = new TCPReceiverThread (node, inSocket);
                Thread thread = new Thread(reqHandler);
                thread.start();
                TCPSender tcpSender = new TCPSender(inSocket);
                // Need to store the TCPsender as well so that message can be sent back.
                // Add connection to an array
                Connections conn = new Connections(inSocket, thread, tcpSender);
//                node.onEvent("connect", inSocket.getInetAddress().getHostName(), conn);
//                node.onEvent("connect", inSocket.getRemoteSocketAddress().toString().split("/")[1], conn);
//                System.out.println("CHECK------======>"+inSocket.getRemoteSocketAddress().toString().split("/")[1]);
//                System.out.println("CHECK------======>"+ inSocket.getRemoteSocketAddress().toString().split("/")[0]+":"+inSocket.getPort());
                node.onEvent("connect",
                        inSocket.getRemoteSocketAddress().toString().split("/")[0]+":"+inSocket.getPort(),
                        conn);
//                System.out.println("SERVER Created thread:: " + Thread.currentThread().getId()+ " Total thread counts: " + Thread.activeCount());
//                System.out.println("OnEvent TCPSERVER:::"+ inSocket.getRemoteSocketAddress());

//                System.out.println("Thread status: "+thread.isInterrupted());
            } catch (IOException e) {
                System.out.println("Error receiving connection.");
                e.printStackTrace();
            }
        }



    }

    public void stopServer(){
        runForever = false;
        try {
            this.myServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort(){
        return myServerSocket.getLocalPort();
    }
    public String getHostName() throws UnknownHostException{
        return InetAddress.getLocalHost().getHostName();
    }
}
