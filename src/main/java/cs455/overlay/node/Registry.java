package cs455.overlay.node;

import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.OverlaySetup;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.*;


public class Registry implements Node{
    /**
     * Foreground Process
     *
     * send different list of messaging nodes to each messaging node in the overlay.
     * Ask messaging node to connect to another node: store the connection info
     * Assign overlay link weights: 1-10 randomly -> send info to registered msg nodes.
     * TASK_INITIATE control msg: inform MN to start sending msg to each other.
     * Rx TASK_COMPLETE from all registered node, issue PULL_TRAFFIC_SUMMARY msg to all MN after X-sec wait.
     *
     * Commands available:
     * - list-messaging-nodes
     * - list-weights
     * - setup-overlay number-of-connections
     * - send-overlay-link-weights
     * - start-number-of-rounds
     *
     *
     */
    private String hostname;
    private String ipAddress;
    private final int port;
    private ArrayList shortestPath;
    private HashMap<String, Connections> connections;
    private HashMap<String, Integer> nodesWeight;
    private HashMap<String, Integer> registeredNodes; // Store node name/id and its socket information.
    private Integer numOfConnections = 4;
    private HashMap<String, String> neighborNodes;
    private HashMap<String, ArrayList> finalOverlay;
    private String[] linkWeights;
    private String[][] linkMap;
    private int countTaskComplete;
    private ArrayList<TrafficSummaryResponse> trafficSummary;

    private ServerSocket myServerSocket;
    private static TCPServerThread TCPServer;

    public Registry(int port){
        this.shortestPath = new ArrayList();
        this.countTaskComplete = 0;
        this.nodesWeight = new HashMap<>();
        this.neighborNodes = new HashMap<>();
        this.registeredNodes = new HashMap<>();
        this.connections = new HashMap<>();
        this.port = port;
        this.trafficSummary = new ArrayList<TrafficSummaryResponse>();
        this.finalOverlay = new HashMap<>();

//        startRegistry();
    }


    private void startRegistry() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            this.hostname = serverSocket.getInetAddress().getLocalHost().getHostName();
            TCPServer = new TCPServerThread(this, serverSocket);
            Thread svrThread = new Thread(TCPServer);
            svrThread.start();
            System.out.println("Started Registry. Registry listening on port "+port);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void stopRegistry(){
        try {
            TCPServer.stopServer();
        } catch (Exception e) {
            System.out.println("Registry:: Error closing socket.");
            e.printStackTrace();
        }

    }

    public  void onEvent(String event, String remoteSocketHostIP, Connections conn){
        // Create object for connection that stores all info.
//        System.out.println("Adding connection to Registry.... " + remoteSocketHostIP);

//        if (event.equals("connect")) {
//            if (!connections.containsKey(remoteHostip)){
                connections.put(remoteSocketHostIP, conn);
//                System.out.println("CONNECTED::" + conn.getServerPort() + " "+conn.getSocket());
//            }
//        }
    }

    // Remove connection which was interrupted without deregister request.
    public  void onEvent(String event, String remoteSocketHostIP){
//        if (event.equals("disconnect")){

            connections.remove(remoteSocketHostIP);
            registeredNodes.remove(remoteSocketHostIP.split(":")[0]);
//            registeredNodes.remove(socket.getPort());
//        }
    }

    private int registerNode(RegisterMessage reg) {
        boolean test = registeredNodes.containsKey(reg.getHostname());
        // TODO - Sometimes registered node missing in list (registered nodes) (DEBUG)
//        System.out.println("INFO: MN:"+reg.getHostname()+" :Port:"+reg.getSocketinfo() + " Server POrt:: "+reg.getServerPort());
        // These two lines were inside if(!test) which might not update the serverport sometime.
        // Check if null (missing serverport fixed) -- YES removed connection put from if check...
        String key = reg.getHostname()+":"+reg.getClientPort();
//        System.out.println(key +" get registered node!!");
        if (connections.containsKey(key)) {
            connections.get(key).setServerPort(reg.getServerPort());
        }
        if (!test) {
            registeredNodes.put(reg.getHostname(), reg.getClientPort() );
            // Sometimes below line gives NullPointerException.
//            registeredNodes.put(reg.getPortNum(),reg.getIpAddress());
            return 0; // Success

        }
        // Check if node already exists.
        if (registeredNodes.get(reg.getHostname()).equals(reg.getClientPort())) {
            return 1; // Failure. Already present in DB
        }
        else{
            return 2; // Failure. Data doesn't match in DB.
        }
//        TODO- Implement error check 2: address in registration request and ip address of request doesn't match.
}

    private void registryHandler(byte[] data){
        /**
         * Registry Request Format:
         *
         * Message Type: 1
         * Ip Address (String)
         * Port Number (int)
         *
         */
        try {
            RegisterMessage regMsg = new RegisterMessage(data);
            String key = regMsg.getHostname()+":"+regMsg.getClientPort();
//            System.out.println("SOCKETINFO IN REGREQ: "+key);
            String msg;
            byte statusCode = 0; // StatusCode = 0 : failure
            int result = registerNode(regMsg);
            if (result == 0){
                statusCode = 1;
                msg = "Registration request successful. The number of messaging nodes currently constituting " +
                        "the overlay is ("+registeredNodes.size()+")";
            }
            else if (result == 1){
//                statusCode = "FAILURE".getBytes();
                msg = "Registration request failed. The messaging node already exists in database.";
//                connections.remove(key);
            }
            else if (result == 2){
//                statusCode = "FAILURE".getBytes();
                msg = "Registration request failed. The entry does not match the database.";
            }
            else{
                msg = "Unknown Error!!";
            }

            // Send Request Response.
            RegisterResponse regResponse = new RegisterResponse(statusCode, msg);
            Connections sender = connections.get(key);
//            System.out.println("Sending Registry Response..."+key+" "+sender.getHostname()+":"+sender.getServerPort()+"="+sender.getClientPort());
            sender.getTcpSender().sendData(regResponse.getBytes());
            System.out.println("Register Response sent to client: "+sender.getHostname());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deregisterHandler(byte[] data){
        try {
            System.out.println("\nProcessing DEREGISTER_REQUEST....\n");
            Deregister deregMsg = new Deregister(data);
            byte statusCode = 0;
            String info;
            // Check where the message originated.
            if (registeredNodes.containsKey(deregMsg.getHostname())){
                System.out.println("\nRemoving entry from registered node.");
                registeredNodes.remove(deregMsg.getHostname());
                statusCode = 1;
                info = "Successful deregistration of IP:port"+deregMsg.getIpAddress()+":"+deregMsg.getPortNum();
            }else{
                System.out.println("ERROR: Hostname not found in registered node list.");
                // Construct DeregisterResponse.
                info = "Failure to deregister IP:port" + deregMsg.getIpAddress()+":"+deregMsg.getPortNum();
            }
            DeregisterResponse deregResponse = new DeregisterResponse(deregMsg.getHostname(),
                                                                        deregMsg.getIpAddress(),
                                                                        deregMsg.getPortNum(),
                                                                        statusCode);
            deregResponse.setInfo(info);
            String key = deregMsg.getHostname()+":"+deregMsg.getPortNum();
            Connections sender = connections.get(key);
            System.out.println("Sending De-registration response...");
//            System.out.println(connections.keySet());
            sender.getTcpSender().sendData(deregResponse.getBytes());
            connections.remove(key);
            // Remove connection information about de-registered node,
            sender.getThread().interrupt();




        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void taskCompleteHandler(byte[] data){
        /* Check when all the nodes has completed the  transfer than only start asking for the traffic summaries.*/
        countTaskComplete++;
//        System.out.println("task complete size : "+ countTaskComplete);

        if (countTaskComplete == registeredNodes.size()){
            // Send request for traffic summary.
            try {
                Thread.sleep(15000); // Wait 15 seconds
                TrafficSummaryRequest trafficReq = new TrafficSummaryRequest();
                for (String key: connections.keySet()){
                    connections.get(key).getTcpSender().sendData(trafficReq.getBytes());
                }
                countTaskComplete = 0;
                } catch (IOException |InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    private  void TrafficSummaryHandler(byte[] data) {
        try {
            TrafficSummaryResponse summary = new TrafficSummaryResponse(data);
            // FOR DEBUG - REMOVE PRINT
//            summary.printData();
            trafficSummary.add(summary);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("Traffic summary size: "+ trafficSummary.size());
        if (trafficSummary.size() == registeredNodes.size()){
            printStat();
            trafficSummary.clear();

        }
    }

    private synchronized void printStat() {
        System.out.println("\t   Number of messages sent | Number of messages received " +
                "| Summation of sent messages | Summation of received messages | Number of messages relayed\n");

        int sumSent = 0;
        int sumReceived = 0;
        long sumSentPayload = 0;
        long sumReceivedPayload = 0;
        int sumRelayed = 0;

        for (int i = 0; i < trafficSummary.size(); ++i){
            System.out.printf("Node %d  | %24d | %27d | %26d | %30d | %10d\n",trafficSummary.get(i).getNodeNumber(),
                    trafficSummary.get(i).getMsgSentTracker(),
                    trafficSummary.get(i).getMsgReceivedTracker(),
                    trafficSummary.get(i).getMsgSentSummation(),
                    trafficSummary.get(i).getMsgReceivedSummation(),
                    trafficSummary.get(i).getMsgRelayedTracker()
                    );
            sumSent += trafficSummary.get(i).getMsgSentTracker();
            sumReceived += trafficSummary.get(i).getMsgReceivedTracker();
            sumSentPayload += trafficSummary.get(i).getMsgSentSummation();
            sumReceivedPayload += trafficSummary.get(i).getMsgReceivedSummation();
            sumRelayed += trafficSummary.get(i).getMsgRelayedTracker();

        }

        System.out.printf("\nSum     | %24d | %27d | %26d | %30d | %10d\n",sumSent, sumReceived,
                sumSentPayload, sumReceivedPayload, sumRelayed);
    }

    public synchronized void onEvent(byte[] data){
        //Add functionality...
        int type=ByteBuffer.wrap(data).getInt();
//        System.out.println("Registry On Event: "+type);

        if(type==1){
            //Registry Request.
            registryHandler(data);
        }

        if (type == 2){
            // Deregister Request
            deregisterHandler(data);
        }

        if (type == 7){
            // Task Complete
            taskCompleteHandler(data);
        }

        if (type == 88){
            TrafficSummaryHandler(data);
//            printStat();
        }
    }

    private void printConnection(){
        System.out.println("Server Connection List: ");
        for (Connections i: connections.values()) {
            System.out.println(i.getHostname()  + ":" + i.getClientPort());
//            System.out.println("REG:: Server Info: " + i.getHostname()  + ":" + i.getServerPort());

        }
    }

    private void printRegisteredNodes(){
        System.out.println("\nList of Registered Nodes: ");
        for (String key: registeredNodes.keySet()){
            System.out.println("Hostname: "+ key + ":"+ registeredNodes.get(key));
        }
//        for (String key: connections.keySet()){
//            System.out.println("Hostname: "+ key);
//        }
    }

    private void setupOverlay(HashMap<Integer, List> overlayMap) {
        String[] nodes = new String[registeredNodes.size()];
        if(registeredNodes.size() == connections.size()) {
            nodes = connections.keySet().toArray(nodes);
        }else{
            // TODO - Handle if all connections doesn't register.
            System.out.println("Restart Registry and start again !!!");
        }

        linkWeights = new String[(int)Math.ceil(numOfConnections*registeredNodes.size()/2)];
        int idx = 0;
        Random rand = new Random();
        for (int i = 0; i < nodes.length; ++i) {
            ArrayList<String> info = new ArrayList<>();
//            System.out.println("Node: "+nodes[i]);

//            System.out.println("Final overlay::"+overlayMap.get(i) + nodes[i]);
//            for (Connections conn: connections.values()) {
//                System.out.println("CONN: "+conn);
//                String targetHost = nodes[i];
            Iterator<Integer> iter = overlayMap.get(i).iterator();
            while (iter.hasNext()) {
                int tmp = iter.next();
                if (tmp > i) {
                    try {
                        String hostname = connections.get(nodes[tmp]).getHostname();
                        Integer svrPort = connections.get(nodes[tmp]).getServerPort();
                        info.add(hostname + ":" + svrPort);
                        String weight = nodes[tmp] + " " + nodes[i] + " " + (rand.nextInt(10) + 1);
                        linkWeights[idx++] = weight;
                    }catch (NullPointerException ne){
//                        connections.clear();
                        System.out.println( "Restart Registry. Stale connection present.");
                    }
                }
            }

            // Store the overlay map.
            finalOverlay.put(nodes[i], info);
//                System.out.println("UPDATED OVERLAY FINALLY::::"+info +"===>"+finalOverlay.size());
            MessagingNodesList msgNodeList = new MessagingNodesList(info.size(), info);
            try {
                connections.get(nodes[i]).getTcpSender().sendData(msgNodeList.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        System.out.println("Overlay setup finished.");

    }

    private void printWeights(){
        LinkWeights linkWt = new LinkWeights(linkWeights.length, linkWeights,numOfConnections);
        linkWt.printData();
    }

    private void sendWeights(){
        LinkWeights linkWt = new LinkWeights(linkWeights.length, linkWeights,numOfConnections);
//        linkWt.printData();
        try {
            for (String conn : connections.keySet()) {
                connections.get(conn).getTcpSender().sendData(linkWt.getBytes());
            }
        } catch(IOException e){
                e.printStackTrace();
        }
        System.out.println("Link Weights sent to Messaging Nodes.");

    }

    private void taskInitiate(int rounds){
        System.out.println("Total message to be sent "+rounds);
        /* Construct the Task Initiate message */
        TaskInitiate task = new TaskInitiate(rounds);
//        task.printData();
        try{
            for(String conn: connections.keySet()){
                connections.get(conn).getTcpSender().sendData(task.getBytes());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Scanner cin = new Scanner(System.in);
        String command;
        boolean overlayCommand = false;
        Registry node = new Registry(Integer.parseInt(args[0]));
        node.startRegistry();
//        System.out.println("Issue command here: ");
        while(true){
            command = cin.nextLine().trim();

            if (command.equals("list-messaging-nodes")){
//            if (command.equals("nodes")){
                node.printRegisteredNodes();

            }
            if (command.equals("connections")){
                node.printConnection();
            }
            if (command.startsWith("setup-overlay")){
//            if (command.startsWith("setup")){
                try {
                    node.numOfConnections = Integer.parseInt(command.trim().split(" ")[1]);
//                    System.out.println("NUM OF CONN: "+ node.numOfConnections);
//                    node.setupOverlay();
                    OverlaySetup obj = new OverlaySetup(node.registeredNodes.size(), node.numOfConnections);
                    obj.setupOverlay();
                    node.setupOverlay(obj.getOverlayMap());
                    overlayCommand = true;
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
            if (command.startsWith("start")){
                try{
                    int rounds = Integer.parseInt(command.trim().split(" ")[1]);
                    node.taskInitiate(rounds*5);

                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
            if (command.equals("send-overlay-link-weights")){
//            if (command.equals("link")){
                if (overlayCommand){
//                    node.assignWeight();
                    node.sendWeights();
                }else{
                    System.out.println("\nFirst use setup-overlay <NumConnection> command to setup overlay.");
                }
            }

            if (command.equals("list-weights")){
                node.printWeights();
            }
            if (command.equals("exit")){
                node.stopRegistry();
                break;
            }

        }
        cin.close();
        System.exit(1);

    }

//     * Commands available:
//            * - list-messaging-nodes
//     * - list-weights
//     * - setup-overlay number-of-connections
//     * - send-overlay-link-weights
//     * - start number-of-rounds


}

