package cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;

public class MessagingNodesList {
    private int type;
    private int peerNodes;
    private ArrayList<String> info = new ArrayList<>() ;

    public MessagingNodesList(int peerNodes, ArrayList info){
        this.type = 4;
        this.peerNodes = peerNodes;
        this.info = info;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(peerNodes);
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

    public MessagingNodesList(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        peerNodes = din.readInt();
        for (int i=0; i< peerNodes; ++i) {
            int identifierLength = din.readInt();
            byte[] identifierBytes = new byte[identifierLength];
            din.readFully(identifierBytes);
            info.add(new String(identifierBytes));
        }

        baInputStream.close();
        din.close();

    }

    public int getPeerNode(){
        return this.peerNodes;
    }
    public ArrayList<String> getInfo(){
        return this.info;
    }

}
