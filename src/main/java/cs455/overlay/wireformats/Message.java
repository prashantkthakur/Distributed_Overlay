package cs455.overlay.wireformats;

import java.io.*;

public class Message {
    private int type;
    private int payload;
    private int dst;
//    private int[] route;

    public Message(int payload, int dst){
        this.type = 6;
        this.dst = dst;
        this.payload = payload;

    }

    public int getPayload(){
        return payload;
    }

    public int getDst(){
        return dst;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(dst);
        dout.writeInt(payload);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public Message(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        dst = din.readInt();
        payload = din.readInt();

        baInputStream.close();
        din.close();

    }
}
