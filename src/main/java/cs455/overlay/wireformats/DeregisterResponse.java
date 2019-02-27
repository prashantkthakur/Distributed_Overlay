package cs455.overlay.wireformats;

import java.io.*;
import java.net.Socket;

public class DeregisterResponse {
    private int type;
    private int portNum;
    private String ipAddress;
    private String hostname;
    private String info;
    private byte statusCode;

    /**
     *
     * info:  Registration request successful. The number of messaging nodes currently constituting the overlay
     *             is (5).
     *             On Failure: Message should say why the registration failed.
     * statusCode: Either Success or Failure.
     *
     */



    // Make the return type common to other messages.
    public DeregisterResponse(String hostname,String ipAddress,int port, byte statusCode){
        this.type = 22; // Value = 2
        this.ipAddress = ipAddress;
        this.portNum = port;
        this.hostname = hostname;
        this.statusCode = statusCode;
    }

    public void setInfo(String info){
        this.info = info;

    }
    public void printData(){
        System.out.println("Message Type: "+ "DEREGISTER_RESPONSE");
        if (this.statusCode == 0) {
            System.out.println("Status Code: FAILURE");
        }
        else{
            System.out.println("Status Code: SUCCESSFUL");
        }
        System.out.println("Additional Info: " + this.info);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeByte(statusCode);

        byte[] identifierBytes = info.getBytes();
        int elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public DeregisterResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        statusCode = din.readByte();

        int identifierLength = din.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        info = new String(identifierBytes);

        baInputStream.close();
        din.close();

    }

}
