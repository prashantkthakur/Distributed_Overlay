package cs455.overlay.connection;
//This is the server program.
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server{
    private ServerSocket myServerSocket;
    private Socket inConnectionSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
//    private Integer PORT_NUM = 5555;
    private Integer NUM_POSSIBLE_CONNECTIONS = 1;

    public void start(Integer PORT_NUM) {
        try {
            //Create the server socket
            myServerSocket = new ServerSocket(PORT_NUM, NUM_POSSIBLE_CONNECTIONS);
        } catch (IOException e) {
            System.out.println("Server::start::Error creating_socket:: " + e);
            System.exit(1);
        }
        try {
            //Bind socket
            inConnectionSocket = myServerSocket.accept();
            System.out.println("Server::start::Connection accepted from "
                    +inConnectionSocket.getRemoteSocketAddress());
            // Create stream
            inputStream = new DataInputStream(inConnectionSocket.getInputStream());
            outputStream = new DataOutputStream(inConnectionSocket.getOutputStream());

            // Send msg
            String msg = "Test!!!";
            byte[] msgToClient = msg.getBytes();
            System.out.println("Server::start:: Sending msg: "+ new String(msgToClient) + ":orig:" + msg);
            Integer msgToClientLength = msgToClient.length;

            //Our self-inflicted protocol says we send the length first
            outputStream.writeInt(msgToClientLength);
            //Then we can send the message
            outputStream.write(msgToClient, 0, msgToClientLength);

            //Now we wait for their response.
            Integer msgLength = inputStream.readInt();

            //If we got here that means there was an integer to
            // read and we have the length of the rest of the next message.
            System.out.println("Received a message length of: " + msgLength);

            //Try to read the incoming message.
            byte[] incomingMessage = new byte[msgLength];
            inputStream.readFully(incomingMessage, 0, msgLength);

            //You could have used .read(byte[] incomingMessage), however this will read
            // *potentially* incomingMessage.length bytes, maybe less.
            //Whereas .readFully(...) will read exactly msgLength number of bytes.

            System.out.println("Received Message: " + new String(incomingMessage));


        } catch (IOException e) {
            System.out.println("Error receiving connection." + e);
        }
    }

    public void stop() {
        try {
            inputStream.close();
            outputStream.close();
            inConnectionSocket.close();
            myServerSocket.close();
        }catch (IOException e) {
            System.out.println("Server::stop::Error closing connection." + e);
            System.exit(1);
        }
    }

    public static void main(String[] args){
        Server myServer = new Server();
        myServer.start(5555);
        myServer.stop();

    }

}