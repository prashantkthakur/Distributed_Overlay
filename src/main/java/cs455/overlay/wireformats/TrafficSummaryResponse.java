package cs455.overlay.wireformats;

import java.io.*;

public class TrafficSummaryResponse {
    private int type;
    private int port;
    private String ipAddress;
    private String hostname;
    private int msgSentTracker;
    private int msgReceivedTracker;
    private int msgRelayedTracker;
    private long msgSentSummation;
    private long msgReceivedSummation;
    private int nodeNumber;

    public TrafficSummaryResponse(int nodeNum, int port, String ipAddress, String hostname,
                          int msgS, long msgSS, int msgR, long msgSR, int msgRly) {
        this.type = 88;
        this.nodeNumber = nodeNum;
        this.port = port;
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.msgReceivedTracker = msgR;
        this.msgRelayedTracker= msgRly;
        this.msgSentTracker = msgS;
        this.msgReceivedSummation = msgSR;
        this.msgSentSummation = msgSS;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public int getType(){
        return type;
    }

    public int getPort(){
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public int getMsgReceivedTracker() {
        return msgReceivedTracker;
    }

    public int getMsgRelayedTracker() {
        return msgRelayedTracker;
    }

    public int getMsgSentTracker() {
        return msgSentTracker;
    }

    public long getMsgReceivedSummation() {
        return msgReceivedSummation;
    }

    public long getMsgSentSummation() {
        return msgSentSummation;
    }

    //    public synchronized void printData(){
    public void printData(){
        System.out.println("Message Type: TRAFFIC_SUMMARY");
        System.out.println("IP Address : " + ipAddress);
        System.out.println("Port Number: " + port);
        System.out.println("Number of message sent : " + msgSentTracker);
        System.out.println("Summation of sent messages: " + msgSentSummation);
        System.out.println("Number of message received : " + msgReceivedTracker);
        System.out.println("Summation of received messages : " + msgReceivedSummation);
        System.out.println("Number of message relayed: " + msgRelayedTracker);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(port);
        dout.writeInt(nodeNumber);

        byte[] identifierBytes = ipAddress.getBytes();
        int elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        identifierBytes = hostname.getBytes();
        elementLength = identifierBytes.length;
        dout.writeInt(elementLength);
        dout.write(identifierBytes);

        dout.writeInt(msgSentTracker);
        dout.writeLong(msgSentSummation);
        dout.writeInt(msgReceivedTracker);
        dout.writeLong(msgReceivedSummation);
        dout.writeInt(msgRelayedTracker);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public TrafficSummaryResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        port = din.readInt();
        nodeNumber = din.readInt();

        int identifierLength = din.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        ipAddress = new String(identifierBytes);

        identifierLength = din.readInt();
        identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        hostname = new String(identifierBytes);

        msgSentTracker = din.readInt();
        msgSentSummation = din.readLong();
        msgReceivedTracker = din.readInt();
        msgReceivedSummation = din.readLong();
        msgRelayedTracker = din.readInt();

        baInputStream.close();
        din.close();

    }
}
