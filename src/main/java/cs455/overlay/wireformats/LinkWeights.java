package cs455.overlay.wireformats;


import java.io.*;
import java.util.ArrayList;

public class LinkWeights {
    private int type;
    private int peerNodes;
    private String[] info ;
    private int numOfConnection;

    public LinkWeights(int totalLinks, String[] info, int numOfConnection){
        this.type = 5;
        this.peerNodes = totalLinks;
        this.info = new String[peerNodes];
        this.info = info;
        this.numOfConnection = numOfConnection;
    }
    public void printData(){
        System.out.println("============================\nWeight Information:\n============================");
        System.out.println("Message Type: Link_Weights");
        System.out.println("Number of links: "+ info.length);
        for (String i: info){
            System.out.println(i);
        }
    }
    public int getNumOfConnection(){
        return numOfConnection;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(peerNodes);
        dout.writeInt(numOfConnection);

        for (String i: info) {
            byte[] identifierBytes = i.getBytes();
            int elementLength = identifierBytes.length;
            dout.writeInt(elementLength);
            dout.write(identifierBytes);
        }


        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public LinkWeights(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        peerNodes = din.readInt();
        numOfConnection = din.readInt();
        info = new String[peerNodes];

        for (int i=0; i< peerNodes; ++i) {
            int identifierLength = din.readInt();
            byte[] identifierBytes = new byte[identifierLength];
            din.readFully(identifierBytes);
            info[i]=new String(identifierBytes);
        }

        baInputStream.close();
        din.close();

    }

    public int getPeerNode(){
        return this.peerNodes;
    }
    public String[] getInfo(){
        return this.info;
    }

}
