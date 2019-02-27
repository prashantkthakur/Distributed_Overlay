//package cs455.overlay.util;
//
//import cs455.overlay.node.MessagingNode;
//
//
//public class TaskInitiationThread implements Runnable {
//    private MessagingNode node;
//    private int rounds;
//
//    public TaskInitiationThread(MessagingNode node, int rounds) {
//        this.node = node;
//        this.rounds = rounds;
//    }
//
//    public void run() {
//        try {
//            node.taskInitiateHandler(rounds);
//            System.out.println("Send task complete...Kill thread.");
//        }finally {
//            Thread.currentThread().interrupt();
//
//        }
//
//    }
//}
