package cs455.overlay.connection;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Client {

//    Scanner input = new Scanner(System.in);
//    private final String SERVER_ADDRESS = ;
//    private final Integer PORT_NUM = input.nextInt();
    Socket socketToServer;
//    DataInputStream inStream;
    DataOutputStream outStream;

    public void start(String hostAddress, Integer portNum){
        try{
            socketToServer = new Socket(hostAddress, portNum);
        }catch (IOException e){
            System.out.println("Client::start::Error connecting to server "+ e);
            System.exit(1);
        }
        while (true) {
            try {
//            inStream = new DataInputStream(socketToServer.getInputStream());
                outStream = new DataOutputStream(socketToServer.getOutputStream());
            } catch (IOException e) {
                System.out.println("Client::start::Error initializing stream " + e);
                System.exit(1);
            }

            try {
                System.out.println("Client::start::Connected to server " + socketToServer.getRemoteSocketAddress());
//            msgLength = inStream.readInt();
//            System.out.println("Client::start::Received msg of length " + msgLength);
//            byte[] inMessage = new byte[msgLength];
//            inStream.readFully(inMessage,0,msgLength);
//            System.out.println("Client::start:: Received message: " + new String(inMessage));
                byte[] msgToServer = ("200;127.0.0.1;"+socketToServer.getPort()).getBytes();
                Integer msgLength = msgToServer.length;
//                outStream.writeInt(200);
                outStream.writeInt(msgLength);
                outStream.write(msgToServer, 0, msgLength);
//                outStream.writeInt(socketToServer.getPort());
                System.out.println("Message sent....");
                socketToServer.getKeepAlive();
            } catch (IOException e) {
                System.out.println("Client::start:: Error sending message to server." + e);
                System.exit(1);
            }
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void stop(){
        try {
            System.out.println("Closing client..." + socketToServer.isConnected());

//            inStream.close();
            outStream.close();
            socketToServer.close();
            System.out.println(socketToServer.isConnected());
        }catch (IOException e){
            System.out.println("Client::stop:: Error stopping service." + e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Client myclient = new Client();
        myclient.start(args[0], Integer.parseInt(args[1]));
//        myclient.stop();


        System.out.println("Done sending msg....client"+myclient.socketToServer.isClosed());

    }
}