package cs455.overlay.wireformats;

import java.io.*;

public class TaskInitiate {
    private int type;
    private int rounds;

    public TaskInitiate(int rounds){
        this.type = 3;
        this.rounds = rounds;
    }

    public void printData(){
        System.out.println("Message Type: TASK_INITIATE");
        System.out.println("Rounds : " + rounds);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(rounds);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public TaskInitiate(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        rounds = din.readInt();

        baInputStream.close();
        din.close();

    }
    public int getRounds(){
        return rounds;
    }
    public int getType(){
        return type;
    }


}