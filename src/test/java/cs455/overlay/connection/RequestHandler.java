package cs455.overlay.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class RequestHandler implements Runnable {

    private Socket inSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public RequestHandler(Socket inSocket){
        try {
            this.inSocket = inSocket;
            inputStream = new DataInputStream(inSocket.getInputStream());
            outputStream = new DataOutputStream(inSocket.getOutputStream());
            System.out.println("Socket registered in ReqHandler..." + inSocket.getRemoteSocketAddress() + inSocket.getLocalSocketAddress());
        }catch (IOException e){
            e.printStackTrace();
        }
        // Create stream

    }

    @Override
    public void run() {
        while(inSocket != null) {
            try {


                System.out.println("Called run in ReqHandler..." + inputStream.available());


//            int availableByte = inputStream.available();
//            if (inputStream.available() != 0) { // Doesn't work for other threads....only works for first thread and then other thread doesn't get input.

                Integer msgLength = inputStream.readInt();
                System.out.println("Received a message length of: " + msgLength);
                byte[] incomingMessage = new byte[msgLength];
                inputStream.readFully(incomingMessage, 0, msgLength);
                System.out.println("Server msg received: " + new String(incomingMessage));
//                    RequestHandler eventHanlder = new EventHandler(incomingMessage);
//                    Thread thread = new Thread(eventHanlder);
//                    thread.start();


//            }
//                System.out.println("Created thread:: " + " " + Thread.currentThread().getName() + " " + Thread.activeCount());
//                System.out.println("ERROR getting stream....."+ Thread.currentThread().isAlive());
                System.out.println("No data in instream....");

            } catch (Exception e) {
                System.out.println("ERROR getting stream.....");
                e.printStackTrace();
                break;
//                Thread.currentThread().interrupt();

            }
        }

            System.out.println("Shutting down thread..." + Thread.currentThread().getId());
            Thread.currentThread().interrupt();

    }


    protected void finalize(){
        try {
//            this.inSocket.close();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
