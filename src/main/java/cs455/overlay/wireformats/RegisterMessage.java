package cs455.overlay.wireformats;

import java.io.*;

public class RegisterMessage {
    private int type;
    private String ipAddress;
    private int serverPort;
    private String hostname;
    private int clientPort;
    private String socketinfo;



    // Make the return type common to other messages.
    public RegisterMessage(String hostname, String socketinfo, int portNum){
        String[] info = socketinfo.trim().split(":");
        this.type = 1;
        this.ipAddress = info[0];
        this.socketinfo = socketinfo;
        this.clientPort = Integer.parseInt(info[1]);
        this.serverPort = portNum;
        this.hostname = hostname;
//        System.out.println("REGMSG:: const::"+socketinfo +"==>"+this.clientPort);

        printMessage();
    }

    public void printMessage(){
        System.out.println("Message Type: "+ this.type);
        System.out.println("IP Address: " + this.ipAddress);
        System.out.println("HOST Address: " + this.hostname);
        System.out.println("Server Port Number: " + this.serverPort);
//        System.out.println("Client Port Number: " + this.clientPort);
    }

    public int getClientPort(){
        return this.clientPort;
    }

    public int getServerPort(){
        return this.serverPort;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public String getHostname(){
        return this.hostname;
    }

    public String getSocketinfo(){return this.socketinfo;}

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(serverPort);
        dout.writeInt(clientPort);


        byte[] identifierBytes = hostname.getBytes();
        int elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        identifierBytes = socketinfo.getBytes();
        elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public RegisterMessage(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        serverPort = din.readInt();
        clientPort = din.readInt();
        int identifierLength = din.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        hostname = new String(identifierBytes);

        identifierLength = din.readInt();
        identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        socketinfo = new String(identifierBytes);

        baInputStream.close();
        din.close();

    }

}
