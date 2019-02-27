package cs455.overlay.wireformats;

import java.io.*;

public class TrafficSummaryRequest {
    private int type;

    public TrafficSummaryRequest(){
        this.type = 8;
    }

    public int getType(){
        return type;
    }

    public void printData(){
        System.out.println("Message Type: PULL_TRAFFIC_SUMMARY");
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public TrafficSummaryRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();

        baInputStream.close();
        din.close();

    }

}

