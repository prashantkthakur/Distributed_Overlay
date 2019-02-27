package cs455.overlay.transport;

import cs455.overlay.node.Connections;
import cs455.overlay.node.Node;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPReceiverThread implements Runnable{

    private Socket socket = null;
    private DataInputStream din;
    private Node node;

    public TCPReceiverThread(Node node, Socket socket) throws IOException {
        this.node = node;
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
    }

    @Override
    public synchronized void run() {
        int dataLength;
        while (socket != null) {
            try {

                dataLength = din.readInt();

                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);
//                System.out.println("TCPReceiverThread:: Data Received: at "+socket.getLocalAddress()+"; data: "+ new String(data));

                // Pass on the data for event handling...
                node.onEvent(data);


            } catch (EOFException eof){
                // If the connection is closed then din.readInt() returns EOFException.
                // If the din.read() is -1, it means the connection was closed by the client so KILL THREAD.
                try {
                    if (din.read() == -1){
//                        System.out.println("!!!!! Close connection !!!!!");
//                        Connections conn = Connections conn = new Connections(node.);
//                        node.onEvent("disconnect", socket.getInetAddress().getHostName(),Integer.toString(socket.getPort()));
                        String info = socket.getRemoteSocketAddress().toString().split("/")[0]+":"+socket.getPort();
                        node.onEvent("disconnect", info);
                        System.out.println("REMOTE DISCONNECTED: "+ info);
                        Thread.currentThread().interrupt();
                        break;
                    }
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            } catch (SocketException se){
                System.out.println("Closed connection to remote server." +se);
                Thread.currentThread().interrupt();
//                se.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
    }

    private void stopThread(){
        Thread.currentThread().interrupt();
    }


}
