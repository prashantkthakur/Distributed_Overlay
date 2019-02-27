package cs455.overlay.wireformats;

import java.io.*;

public class Deregister {

    private int type;
    private String ipAddress;
    private int portNum;
    private String hostname;

    public Deregister(String hostname, String ipAddress, int port){
        this.type = 2; // Value = 2
        this.ipAddress = ipAddress;
        this.portNum = port;
        this.hostname = hostname;

    }
    public void printMessage(){

        System.out.println("Message Type: " + "DEREGISTER_REQUEST");
        System.out.println("IP Address: " + this.ipAddress);
        System.out.println("Port Number: " + this.portNum);
    }
    public int getType(){
        return type;
    }
    public String getIpAddress(){
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPortNum() {
        return portNum;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(portNum);

        byte[] identifierBytes = hostname.getBytes();
        int elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        identifierBytes = ipAddress.getBytes();
        elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public Deregister(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        portNum = din.readInt();

        int identifierLength = din.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        hostname = new String(identifierBytes);

        identifierLength = din.readInt();
        identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        ipAddress = new String(identifierBytes);

        baInputStream.close();
        din.close();

    }

}
