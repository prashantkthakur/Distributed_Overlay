package cs455.overlay.wireformats;

import java.io.*;

public class TaskComplete {
    private int type = 7;
    private String ipAddress;
    private int port;
    private String hostname;

    public  TaskComplete(int port, String hostname, String ipAddress){
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void printData() {
        System.out.println("Message Type: TASK_COMPLETE");
        System.out.println("Node IP Address: " + ipAddress);
        System.out.println("Node Port: " + port);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(port);

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

    public TaskComplete(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        port = din.readInt();

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
