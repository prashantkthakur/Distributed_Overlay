//package cs455.overlay.node;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.util.Random;
//
//class ServerInitializer extends Thread{
//
//    private ServerSocket myServerSocket;
//    private Integer PORT_NUM = 0;
//    private Thread thread;
//    private boolean running;
//
//    public void startServer() {
////        while (true) {
////            try {
//////                Random rand = new Random();
//////                PORT_NUM = rand.nextInt((65535 - 1025) + 1) + 1025; // Random selection of port
////                PORT_NUM = 5555; // TESTING...
////
////                //Create the server socket
////                myServerSocket = new ServerSocket(PORT_NUM); // add zero...
////                System.out.println("Listening on port " + PORT_NUM);
////                if (myServerSocket.isBound()) {
////                    break;
////                }
////            } catch (IOException e) {
////                System.out.println("Attempt to connect to port " + PORT_NUM + " failed. " + e.toString());
////
////            }
////        }
////
////        try {
////            myServerSocket = new ServerSocket(PORT_NUM);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        running = true;
////        while (running) {
////            try {
////                //Bind socket
////                ClientHandler clientHandle = new ClientHandler(myServerSocket.accept());
////                thread = new Thread(clientHandle);
////                System.out.println("Created thread:: " + Thread.currentThread().getId() + " Total thread counts: " + Thread.activeCount());
////                thread.start();
//////                if(!thread.isAlive()){
//////                    try {
//////                        thread.join();
//////                        thread.interrupt();
//////                    }catch (InterruptedException e) {
//////                        e.printStackTrace();
//////                    }
//////            }
////
////            } catch (Exception e) {
////                System.out.println("Error receiving connection." + e);
////            }
//        }
////        stopServer();
//    }
//    public void stopServer() {
//        try {
//            running = false;
//            myServerSocket.close();
//
//        } catch (IOException e) {
//            System.out.println("Server::stop::Error closing connection." + e);
//            System.exit(1);
//
//        }
//    }
//
//    protected void finalize() throws InterruptedException {
//        thread.join();
//        thread.interrupt();
//    }
//
//}
