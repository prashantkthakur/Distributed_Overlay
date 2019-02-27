package cs455.overlay.node;

import cs455.overlay.dijkstra.ShortestPath;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessagingNode implements Node {
    /**
     * Foreground process
     *
     * Successful conxn to another node: send success msg.
     * Rx weight info: store info to generate routing path & prints msg to console.
     * Ready to send pkt: compute shortest path & create pck with payload to send.
     * Same pck should not be received more than once at a node. HOW?
     * Msg sent in rounds: send task completion msg to RN.
     * Rx PULL_TRAFFIC_SUMMARY: create response and send to RN & reset the counters associated with traffic.
     *
     * Commands available:
     * - print-shortest-path
     * - exit-overlay
     *
     *
     */

    private String registryAddress;
    private int registryPort;
    private int msgSentTracker;
    private int msgReceivedTracker;
    private int msgRelayedTracker; // Msg was received and sent to desired route.
    private long msgSentSummation; // Sum of all sent pkts payload's value.
    private long msgReceivedSummation; // Sum the value of payloads received.
    private int messageNodePort;
    private TCPServerThread TCPServer;
    private Socket socketToRegistry;
    private HashMap<String, Socket> msgNodeList;
    private String hostname;
    private String hostIP;
    private int clientPort;
    private String[] linkWeights;
    private ServerSocket serverSocket;
    private HashMap<String, Connections> messagingClients;
    private ArrayList<String> allMsgNodes;
    private int[][] weightMatrices;
    private int[] path;
    private Lock lock = new ReentrantLock();

    private MessagingNode(String registryAddress, int registryPort){

        this.registryAddress = registryAddress;
        this.registryPort = registryPort;
        this.clientPort = 0;
        this.messageNodePort = 0; // 0 means take any port that is available.
        this.messagingClients = new HashMap<>();
        this.msgNodeList = new HashMap<>();
        resetTrafficCounters();

    }

    private void startServer(){
        try {
            serverSocket = new ServerSocket(messageNodePort);
//            hostname = serverSocket.getInetAddress().getLocalHost().getHostName();
            hostIP = serverSocket.getInetAddress().getLocalHost().getHostAddress();
            messageNodePort = serverSocket.getLocalPort();
            System.out.println("Started Messaging Node ::   " + hostIP + ":" + messageNodePort);
            TCPServer = new TCPServerThread(this, serverSocket);
            Thread svrThread = new Thread(TCPServer);
            svrThread.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void stopServer(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean sendRegistryRequest(){
        // Create thread for TCPSender to send data for register request.
        try {
//            this.hostname = socketToRegistry.getLocalAddress().getHostName();
            this.hostname = socketToRegistry.getLocalAddress().getCanonicalHostName();
            TCPSender tcpSender = new TCPSender(socketToRegistry);
            // Create message to send client information from Messaging Node.
//            RegisterMessage regMsg = new RegisterMessage(1,
//                    hostname,
//                    socketToRegistry.getLocalSocketAddress().toString().replaceFirst("/",""),
//                    socketToRegistry.getLocalPort());

            // TODO - Sometimes null is sent to Registry which cause connection serverPort to be null.
//            System.out.println("Server port to send RegistryRequest: "+ serverSocket.getLocalPort());
            RegisterMessage regMsg = new RegisterMessage(hostname,
                    socketToRegistry.getLocalSocketAddress().toString().trim().replaceFirst("/",""),
                    serverSocket.getLocalPort());
            tcpSender.sendData(regMsg.getBytes()); // Send the byte[] message to server.
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    private void initialize(){
        startServer();
        register();
    }

    private void register(){
        try{
            socketToRegistry = new Socket(registryAddress, registryPort);
            // Create thread for TCPReceiver to receive information from registry.
            TCPReceiverThread tcpRcvr = new TCPReceiverThread(this, socketToRegistry);
            this.clientPort = socketToRegistry.getLocalPort();
//            System.out.println("Client socket MN:::"+ socketToRegistry.getLocalPort());
            Thread rcvrThread = new Thread(tcpRcvr);
            rcvrThread.start();
            if (sendRegistryRequest()){
                System.out.println("Registry Request SENT " + this.registryAddress + ":" + this.registryPort);
            }else{
                System.out.println("Registry Request FAILED " + this.registryAddress + ":" + this.registryPort);
            }

        }catch (IOException e){
//            System.out.println("Messaging Node "+ nodeNumber + ": Error connecting to server " + e);
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }

    private void deregister() {
        System.out.println("Sending deregister request.");
        try{
            Deregister dmsg = new Deregister(this.hostname, this.hostIP, this.clientPort);
            TCPSender tcpSender = new TCPSender(socketToRegistry);
            tcpSender.sendData(dmsg.getBytes());
            System.out.println("Deregister Request completed. Exiting this Messaging Node.");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                socketToRegistry.shutdownInput();
                socketToRegistry.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void resetTrafficCounters(){
        this.msgSentTracker = 0;
        this.msgRelayedTracker = 0;
        this.msgReceivedTracker = 0;
        this.msgSentSummation = 0;
        this.msgReceivedSummation = 0;
    }

    public  void onEvent(String event, String remoteHostip, Connections conn){
        // Create object for connection that stores all info.
        System.out.println("Adding connection to MsgNode.... " + remoteHostip);

        if (event.equals("connect")) {
//            if (!connections.containsKey(remoteHostip)){
            messagingClients.put(remoteHostip, conn);
//            System.out.println("CONNECTED:: " + conn.getSocket());
//            }
        }
    }

    public void onEvent(String event, String socketHost){
//        if (event.equals("disconnect")){
//            messagingClients.remove(socket.getRemoteSocketAddress().toString());
            messagingClients.remove(socketHost);
//            System.out.println("MN DISCONNECT::: Remote Add: "+ socket.getInetAddress().getHostAddress());
//            System.out.println("MN DISCONNECT::: Local Add:  "+ socket.getLocalAddress());
//            System.out.println("MN DISCONNECT::: Hostname:  "+ socket.getInetAddress().getHostName());
//            registeredNodes.remove(socket.getInetAddress().getHostName());
//            registeredNodes.remove(socket.getPort());
//        }
    }

    /* IF NOT SYNCHRONIZED Gives:
        Exception in thread "Thread-2" java.nio.BufferUnderflowException
        at java.nio.Buffer.nextGetIndex(Buffer.java:506)
        at java.nio.HeapByteBuffer.getInt(HeapByteBuffer.java:361)
        at cs455.overlay.node.MessagingNode.onEvent(MessagingNode.java:208)
        at cs455.overlay.transport.TCPReceiverThread.run(TCPReceiverThread.java:37)
        at java.lang.Thread.run(Thread.java:748) */
    @Override
    public synchronized void onEvent(byte[] data) {
        int type=ByteBuffer.wrap(data).getInt();
//        System.out.println("Messaging Node On Event: "+type);


        if (type == 3){
            System.out.println("\n======================\nInitiating Task\n======================");
            taskInitiateHandler(data);
        }

        // MessagingNodeList received from registry.
        if(type==4){
            System.out.println("\n======================\nSetting up overlay!!!\n============================");
            messageNodeListHandler(data);
            try {
                // Minor sleep to make the connection stable and registered in the Data Structure.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("All connections are established between messaging nodes. Number of connections: " + (msgNodeList.size()+messagingClients.size()));

        }

        // Link Weights received from registry.
        if(type == 5){
            linkWeightsHandler(data);
        }

        if (type == 6){
            messageReceived(data);
        }

        if (type == 8){
            // Pull_Traffic_Summary request.
            TrafficSummaryHandler();
        }


    }

    private void messageNodeListHandler(byte[] data){
        try {
            MessagingNodesList msg = new MessagingNodesList(data);
//            System.out.println("PEER NODE: "+msg.getPeerNode());
//            System.out.println("INFO: "+msg.getInfo());
            for(String info: msg.getInfo()) {
                String[] messagingNodeServer = info.split(":");
                String host = messagingNodeServer[0];
                int port = Integer.parseInt(messagingNodeServer[1]);
                Socket socketToMN = new Socket(host, port);
                // Create thread for TCPReceiver to receive information from another MN.
                TCPReceiverThread mnRcvr = new TCPReceiverThread(this, socketToMN);
//                System.out.println("OVERLAY MN:::" + socketToMN.getLocalPort());
                Thread rcvrThread = new Thread(mnRcvr);
                rcvrThread.start();
                msgNodeList.put(info, socketToMN);

            }

        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(1);
        }


    }

    private void listConnections(){
        System.out.println("\n=====================\nClients List on MN\n===================");
        for (String i: messagingClients.keySet()){
            System.out.println("Client: "+i );
        }

    }

    private void computeShortestPath() {
        System.out.println("Computing shortest Path...");
        String srcKey = hostname + ":" + clientPort;
        System.out.println(srcKey); // item present in the allMsgNodes. Use as key
        // Gives : acorn:5555 - DON't USE BELOW LINE AS KEY
        // System.out.println(socketToRegistry.getInetAddress().getHostName()+":"+this.socketToRegistry.getPort());
        int srcIdx = allMsgNodes.indexOf(srcKey);

       /* Check by passing custom network weight map graph */
        // // Test if the path returned is correct?
//        // 0 means no connection between the nodes.
//        int graph[][] = new int[][]{
//                {0, 4, 3, 0, 5, 4, 0, 0, 0, 0},
//                {4, 0, 5, 1, 10, 0, 0, 0, 0, 0},
//                {3, 5, 0, 0, 0, 1, 5, 0, 0, 0},
//                {0, 1, 0, 0, 2, 0, 0, 0, 1, 10},
//                {5, 10, 0, 2, 0, 0, 0, 0, 0, 5},
//                {4, 0, 1, 0, 0, 0, 10, 10, 0, 0},
//                {0, 0, 5, 0, 0, 10, 0, 10, 8, 0},
//                {0, 0, 0, 0, 0, 10, 10, 0, 1, 5},
//                {0, 0, 0, 1, 0, 0, 8, 1, 0, 9},
//                {0, 0, 0, 10, 5, 0, 0, 5, 9, 0}
//        };
//        ShortestPath sp = new ShortestPath(graph.length);
//        sp.computePath(graph, 9);
//        sp.printSolution();
        ShortestPath sp2 = new ShortestPath(weightMatrices.length);
        sp2.computePath(weightMatrices, srcIdx);
        System.out.println("Accessing path.");
        sp2.printPath();
        /* Check if the path getter() returns same data */
        //for(int val: sp2.getPath()){
//            System.out.print(val +" ");
//        }
//        System.out.println("Accessing Distance.");
//        for(int val: sp2.getDist()){
//            System.out.print(val +" ");
//        }

//        System.out.println("\nINDEX of this node: " + srcIdx);
        path = sp2.getPath();
        System.out.println("Shortest Path computed !!");

    }

    private void linkWeightsHandler(byte[] data){
        try {
            LinkWeights linkWt = new LinkWeights(data);
            linkWeights = new String[linkWt.getPeerNode()];
            linkWeights = linkWt.getInfo();
            int numOfNodes = linkWeights.length* 2 / linkWt.getNumOfConnection();
            allMsgNodes = new ArrayList<>(numOfNodes);
            weightMatrices = new int[numOfNodes][numOfNodes];
            /* Changed implementation where 0 means no connection between nodes */
            //
            //Initialize higher value for the weight map. Just in case there is no connection between nodes.
//            for (int i = 0; i < numOfNodes; i++) {
//                for (int j = 0; j < numOfNodes; j++){
////                    if (i == j){
////                        weightMatrices[i][j] = 0;
////                    }else{
//                        weightMatrices[i][j] = 0; // too expensive path for Dijkstra's algo.
////                    }
//                }
//            }

            /* Create weightMap for network weight. */
            for (String val: linkWeights){
                String[] splits = val.split(" ");
                if (!allMsgNodes.contains(splits[0])){
                    allMsgNodes.add(splits[0]);
                }
                if (!allMsgNodes.contains(splits[1])){
                    allMsgNodes.add(splits[1]);
                }
                int node1 = allMsgNodes.indexOf(splits[0]);
                int node2 = allMsgNodes.indexOf(splits[1]);

                    weightMatrices[node1][node2] = weightMatrices[node2][node1] = Integer.parseInt(splits[2]);

            }


            computeShortestPath();
            // Requirement to display.
            System.out.println("Link weights are received and processed. Ready to send messages.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* Get nodes that is the nearest towards the destination  */
    private int getNextNode(int src, int dst){
        int prev = path[dst];
        int result= dst;
        if ( prev != src){
            result = getNextNode(src,prev);
//            return dst;
        }
        return result; // Never reaches this point.
    }

    private void TrafficSummaryHandler() {
        /* Send traffic summary to Registry */

        try {
            /* TODO- Only do this if the relay buffer is reset to 0.
             * Conclusion: Can't tell if the packet is in route.
             * Routing node has no idea about the packet will be received. */

            System.out.println("Sending Traffic Summary..");
            String srcKey = hostname + ":" + clientPort;
            int srcIdx = allMsgNodes.indexOf(srcKey);
            TrafficSummaryResponse trafficSummary = new TrafficSummaryResponse(srcIdx, clientPort, hostIP, hostname,
                    msgSentTracker, msgSentSummation, msgReceivedTracker, msgReceivedSummation, msgRelayedTracker);
            TCPSender tcpSender = new TCPSender(socketToRegistry);
            tcpSender.sendData(trafficSummary.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Reset Counters after sending traffic information.
        resetTrafficCounters();
//                routeQueue = 0;
    }


    private  void messageReceived(byte[] data){
        try {
            lock.lock();
            Message msg = new Message(data);
            String srcKey = hostname + ":" + clientPort;
            int srcIdx = allMsgNodes.indexOf(srcKey);
            if (msg.getDst() != srcIdx){
                /* Forward Data to the route */
//                System.out.println("MSG ROUTE::DIFF: DST: "+ msg.getDst() + " SRC: "+srcIdx);
//                routeMessage(msg.getDst(), msg);
//                String srcKey = hostname + ":" + clientPort;
//                int srcIdx = allMsgNodes.indexOf(srcKey);
                int relayNode = getNextNode(srcIdx, msg.getDst());
//            System.out.println("ROUTING MSG::: DST: "+dst +" SRC: "+srcIdx+ " RelayNode: "+relayNode);
                String remoteHost = allMsgNodes.get(relayNode).trim().split(":")[0];
                for (String key : messagingClients.keySet()) {
                    if (key.startsWith(remoteHost)) {
                        messagingClients.get(key).getTcpSender().sendData(msg.getBytes());
                        msgRelayedTracker++;
                    }
                }
                for(String key: msgNodeList.keySet()) {
                    if (key.startsWith(remoteHost)) {
//                    System.out.println("###Routed from msgNODELIST...");
                        TCPSender tcpSender = new TCPSender(msgNodeList.get(key));
                        tcpSender.sendData(msg.getBytes());
                        msgRelayedTracker++;
                    }
                }
            }
            else{
                    /*This is your data */
                    // TODO- LOCK start

//                    System.out.println("Update RECEIVED: SAME:: DST: " + msg.getDst() + " Payload: " + msg.getPayload());
                    msgReceivedTracker++;
                    msgReceivedSummation += msg.getPayload();
//                    System.out.println("Done updating received counters****" + msgReceivedSummation + "::traker:: " + msgReceivedTracker);
                    // TODO- UNLOCK


            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    private void printStat() {
        System.out.println("Sent: "+msgSentTracker);
        System.out.println("Received: "+msgReceivedTracker);
        System.out.println("Sent Sum: "+msgSentSummation);
        System.out.println("Received Sum: "+msgReceivedSummation);
        System.out.println("Relayed: "+msgRelayedTracker);

    }

    private void taskInitiateHandler(byte[] data){
        try {
            // TODO-Start lock!!

            TaskInitiate task = new TaskInitiate(data);
            Random rand = new Random();
            String srcKey = hostname + ":" + clientPort;
            int srcIdx = allMsgNodes.indexOf(srcKey);
            int totalRounds = task.getRounds();
            for (int i = 0; i < totalRounds; ++i) {
                int payload = rand.nextInt();
//                int randDestination = srcIdx;
                int randDestination = rand.nextInt(allMsgNodes.size());
                if (randDestination == srcIdx) {
                    randDestination = (randDestination + 1) % allMsgNodes.size();
                }
                Message msg = new Message(payload, randDestination);

                // Send message to the nearest node for forwarding.
//                System.out.println("SRCIDX: "+ srcIdx+ " DST: "+randDestination);
                int relayNode = getNextNode(srcIdx, randDestination);
                /* TODO- MAKE CHANGE SO THAT MN ON SAME HOST ARE SUPPORTED */
//                System.out.println("Relay node: "+relayNode);
//                System.out.println("Getting HOSTNAME: "+ allMsgNodes.get(relayNode).trim().split(":")[0]);
//                System.out.println("===> Preparing to send msg: Rand Dst: "+ randDestination + " Relay: "+relayNode);
                String remoteHost = allMsgNodes.get(relayNode).trim().split(":")[0];
//                try {
//                    lock.lock();
                for (String key : messagingClients.keySet()) {
//                        System.out.println("Size of client:" + messagingClients.size());
//                        System.out.println("Key: " + key + " remote host: " + remoteHost + " DST node: " +
//                                randDestination + "DST host: " + allMsgNodes.get(randDestination));
                    if (key.startsWith(remoteHost)) {
//                            System.out.println("###SENT MSG");
                        messagingClients.get(key).getTcpSender().sendData(msg.getBytes());
                        msgSentSummation += payload;
                        msgSentTracker++;
                    }
                }
                for (String key : msgNodeList.keySet()) {
                    if (key.startsWith(remoteHost)) {
//                            System.out.println("###SENT MSG from msgNODELIST...");
                        TCPSender tcpSender = new TCPSender(msgNodeList.get(key));
                        tcpSender.sendData(msg.getBytes());
                        msgSentSummation += payload;
                        msgSentTracker++;
                    }
                }
//                }finally {
//                    lock.unlock();
//                }
            }

            /* TODO- Is sleep needed? */
            System.out.println("Sending Message task complete.");
            System.out.println("Sending TASK_COMPLETE Response to Registry.");
            TaskComplete tc = new TaskComplete(clientPort, hostname, hostIP);
            TCPSender tcpSender = new TCPSender(socketToRegistry);
            tcpSender.sendData(tc.getBytes());

        } catch (IOException  e) {
            e.printStackTrace();
        }

    }
/* inlined the method in receivedMessageHandler */
//    private synchronized void routeMessage(int dst, Message msg){
//        // TODO- LOCK start
//        try {
////            lock.lock();
////            Random rand = new Random();
//            String srcKey = hostname + ":" + clientPort;
//            int srcIdx = allMsgNodes.indexOf(srcKey);
//            int relayNode = getNextNode(srcIdx, dst);
////            System.out.println("ROUTING MSG::: DST: "+dst +" SRC: "+srcIdx+ " RelayNode: "+relayNode);
//            String remoteHost = allMsgNodes.get(relayNode).trim().split(":")[0];
//            for (String key : messagingClients.keySet()) {
//                if (key.startsWith(remoteHost)) {
//                    messagingClients.get(key).getTcpSender().sendData(msg.getBytes());
//                    msgRelayedTracker++;
//                }
//            }
//            for(String key: msgNodeList.keySet()) {
//                if (key.startsWith(remoteHost)) {
////                    System.out.println("###Routed from msgNODELIST...");
//                    TCPSender tcpSender = new TCPSender(msgNodeList.get(key));
//                    tcpSender.sendData(msg.getBytes());
//                    msgRelayedTracker++;
//                }
//            }
//        }catch (IOException e){
//            e.printStackTrace();
////        }finally {
////            lock.unlock();
//        }
//
//    }

    /* Temporary method to check if the clients from the MessagingNodes can be reached.. */
    private void exchangeData(){

        String MSG = "Message from server "+ hostname;
        for(String val: allMsgNodes){
            System.out.println("All messaging nodes list: "+val);
        }
        for (String i: messagingClients.keySet()){
                System.out.println("MN connected to "+hostname+ "::=> "+i+ "    "+messagingClients.get(i).getServerPort());
//                messagingClients.get(i).getTcpSender().sendData(MSG.getBytes());
        }
        TaskInitiate taskInit = new TaskInitiate(1);
        try {
            taskInitiateHandler(taskInit.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getWeight(int node1, int node2){
        return weightMatrices[node1][node2];
    }

    private void printPathHelper(int start, int end){

        if(end == start){
            System.out.println(allMsgNodes.get(end).replace(".cs.colostate.edu",""));
            return;
        }

        else{
            System.out.print(allMsgNodes.get(start).replace(".cs.colostate.edu","") + "--"+getWeight(start,path[start])+"--");
            printPathHelper(path[start],end);
        }

    }

    private void printShortestPath(){
        /**
         * Should print shortest path.
         * Example:
         *
         * dover:port--8--salem:port--2--denver:port--4--uno:port
         * salem:port--1--richmond:port--2--uno:port
         * denver:port--4--uno:port
         */

        String srcKey = hostname + ":" + clientPort;
        int srcIdx = allMsgNodes.indexOf(srcKey);
        for(int i= 0; i < allMsgNodes.size(); ++i){
            if (i == srcIdx){
                continue;
            }
            printPathHelper(i,srcIdx);

        }
    }

    public static void main(String[] argv){
        String ipAdd = argv[0];
        int port = Integer.parseInt(argv[1]);
        MessagingNode node = new MessagingNode(ipAdd, port);
        node.initialize();

        Scanner cin = new Scanner(System.in);
        String command;
        while(true){
            command = cin.nextLine();

            if (command.equals("exit-overlay")){
                node.deregister();
                break;
            }

            // if (command.equals("path")){
            if (command.equals("print-shortest-path")) {
                node.printShortestPath();
            }

            /* Some additional functions to check progress */
            if (command.equals("deregister")){
                node.deregister();

            }
            if(command.equals("connections")){
                node.listConnections();
            }

            if (command.equals("exchange")){
                node.exchangeData();
            }


            if (command.equals("stat")){
                node.printStat();
            }



        }
        cin.close();

    }



}
