# Distributed_Systems

## Start Master (Registry) Node:
`java cs455.overlay.node.Registry portnum`

### Commands available:

#### list-messaging-nodes       
This should result in information about the messaging nodes (hostname, and port-number)  being listed. Information for each messaging node should be listed on a separate line.

#### list-weights      
This should list information about links comprising the overlay. Each link’s information should be on a separate line and include information about the nodes that it connects and the weight of that link. For example, carrot.cs.colostate.edu:2000 broccoli.cs.colostate.edu:5001 8, indicates that the link      is      between      two      messaging      nodes      (carrot.cs.colostate.edu:2000)      and      (broccoli.cs.colostate.edu:5001) with a link weight of 8.

#### setup-overlay number-of-connections
This  should  result  in  the  registry  setting  up  the  overlay.  It  does  so  by  sending  messaging  nodes  messages  containing  information  about  the  messaging  nodes  that  it  should  connect  to.  The  registry  tracks  the  connection  counts  for  each  messaging  node  and  will  send  the  MESSAGING_NODES_LIST message to every messaging node. A sample specification of this command is setup-overlay 4 that will result in the creation of an overlay where each messaging node is connected to exactly 4 other messaging nodes in the overlay. You should handle the error condition where the number of messaging nodes is less than the connection limit that is specified.NOTE: You are not required to deal with the case where a messaging node is added or removed after the overlay has been set up. You must however deal with the case where a messaging node registers and deregisters from the registry before the overlay is set up. 

#### send-overlay-link-weights

This should result in a Link_Weights message being sent to all registered nodes in the overlay.  This command is issued once after the setup-overlay command has been issued.  This also allows all nodes in the system to be aware of not just all the nodes in the system, but also the complete set of links in the system.

#### start number_of_rounds
Send messages to random messaging nodes.



## Starting Messaging nodes

`java cs455.overlay.node.MessagingNode registry-host registry-port`

print-shortest-path   
This  should  print  the  shortest  paths  that  have  been  computed  to  all  other  the  messaging  nodes  within the system. The listing should also indicate weights associated with the links. e.g.  carrot–-8––broccoli––4––-zucchini––-2––brussels––1––onionexit-overlayThis  allows  a  messaging  node  to  exit  the  overlay.  The  messaging  node  should  first  send  a  deregistration  message  to  the  registry  and  await  a  response  before  exiting  and  terminating the process.

exit-overlay
Command to deregister from the Master/Registry node.

-------------------

### Section I: Source Code Information

-------------------

#### Package: node

1) Registry :
    This class handles all the functionality of the Registry. The registry waits 15 seconds before sending a
    request to the Messaging nodes to send traffic information. There are three onEvent() methods with different
    number of arguments. One is use to store the connection made to the registry to store the connections information
    from MN (messaging nodes). One is to track if the messaging node is disconnected to remove the connection.
    Another is to handle/call different function based on the type of message received from the TCPReceiverThread.

    Additional command: connections => list all the connection information to the Registry server.

2) Node :
        This is an abstract class for the nodes.

3) MessagingNode :
    This contains the functionality of Messaging Nodes. It has similar onEvent methods with same functionality as Registry.

4) Connections :
    This class stores the connection information generated by the TCPServerThread class. It stores the information
    about the socket (hostname:port) connected to the server, the TCPReceiver thread that was created for the connection
    and the TCPSender instance. This is later used to access those connections and send data.

#### Package: dijkstra

1) ShortestPath :
    This gives the shortest path for the network generated by Registry based on the weight assigned for each connections.
    The Dijkstra's algorithm is implemented where the network matrices is constructed with the weight for the given
    connection and 0 for no connection between nodes. Example: 1 : [2, 0, 0, 3, 0, 0, 8, 4, 0, 10] For second node, it has
    connection with 1st, 4th, 7th, 8th and 10th node with the corresponding weights.

#### Package: transport

1) TCPServerThread :
    Listens for the incoming connections and creates TCPReceiver thread and TCPsender instance and pass it to the Server
    (either Registry or MN) with all those information by calling onEvent() method (that could have been named different).

2) TCPReceiverThread :
    Pass the data to nodes by calling onEvent(byte[] data) method. It checks if the connection to any host is disconnected
    if that's the case, it send disconnect information to nodes to remove it from their list.

3) TCPSender : Sends the data in byte format.

#### Package: utils

1) OverlaySetup :
    This is used to create an overlay network based on the number of connection input. Generates different connections between
    different number of nodes.

#### Package: wireformats

   All the classes support the functionality of different types of messages that needs to be passed around in the nodes
    for different events like registration, de-registration, task initiation, task completion etc. Most of them has similar
    code structure to marshall and unmarshall the object.
    Type of each message format is directly coded in corresponding constructor for that class.

