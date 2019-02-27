package cs455.overlay.util;


import java.util.*;

public class OverlaySetup {
    private int totalNodes;
    private int[] nodes;
    private int numOfConnections;
    private HashMap<Integer,List> overlayMap;

    public OverlaySetup(int totalNodes, int numOfConnections) {
        this.totalNodes = totalNodes;
        nodes = new int[totalNodes];
        for (int i = 0; i < totalNodes; ++i) {
            nodes[i] = i;
        }
        this.numOfConnections = numOfConnections;
        this.overlayMap = new HashMap<>();

    }

    public HashMap<Integer,List> getOverlayMap(){
        return this.overlayMap;
    }

    public void setupOverlay() {
        if (numOfConnections < 2){
            System.out.println("Number of connections must be greater than 1.");
            return;
        }

        if ((numOfConnections * nodes.length % 2 != 0) || nodes.length >= (numOfConnections + 1)) {
            int[] counter = new int[nodes.length];

            for (int i = 0; i < nodes.length; ++i) {
                overlayMap.put(i, new ArrayList<>());
            }
            for (int i = 0; i < nodes.length; ++i) {
                if (!overlayMap.containsKey(i)) {
                    overlayMap.put(i, new ArrayList<>(Arrays.asList((i + 1) % nodes.length)));
                    overlayMap.put((i + 1) % nodes.length, new ArrayList<>(Arrays.asList(i)));
                    counter[i]++;
                    counter[(i + 1) % nodes.length]++;
//                    System.out.println("IF " + overlayMap.get(i) + ":::" + overlayMap.get((i + 1) % nodes.length));

                } else if ((i + 1) % nodes.length != 0) {
                    overlayMap.get(i).add((i + 1) % nodes.length);
                    overlayMap.put((i + 1) % nodes.length, new ArrayList<>(Arrays.asList(i)));
                    counter[i]++;
                    counter[(i + 1) % nodes.length]++;
//                    System.out.println("ELIF " + overlayMap.get(i) + ":::" + overlayMap.get((i + 1) % nodes.length));

                } else {
                    overlayMap.get(i).add((i + 1) % nodes.length);
                    overlayMap.get((i + 1) % nodes.length).add(i);
                    counter[i]++;
                    counter[(i + 1) % nodes.length]++;
//                    System.out.println("ELSE " + overlayMap.get(i) + ":::" + overlayMap.get((i + 1) % nodes.length));

                }
            }

            // two connection has been established...
            if (numOfConnections == 2) {
                return;
            }
            int connection;
            if (numOfConnections %2 == 0){
                connection = numOfConnections-1;
            }else {
                connection = numOfConnections;
            }
            while (true) {
                if (connection == nodes.length/2 && numOfConnections %2 == 0) {
                    connection--;
                }
                int check = checkConnections(counter);
                if (check == numOfConnections){
//                    System.out.println("Setup of Overlay completed...");
                    break;
                }
                boolean twoAtOnce = (numOfConnections - check > 1);
                for (int i = 0; i < nodes.length; ++i) {
                    if (twoAtOnce) {
                        if (!overlayMap.get(i).contains((i + connection) % nodes.length) && !overlayMap.get((i + connection) % nodes.length).contains(i)) {
                            overlayMap.get(i).add((i + connection) % nodes.length);
                            overlayMap.get((i + connection) % nodes.length).add(i);
                            counter[i]++;
                            counter[(i + connection) % nodes.length]++;
                        }
                    } else {
                        if (!overlayMap.get(i).contains((i + connection) % nodes.length)) {
                            overlayMap.get(i).add((i + connection) % nodes.length);
                            counter[i]++;
                        }
                    }
                }
//                System.out.println("Returned value: "+checkConnections(counter));
                if (numOfConnections - checkConnections(counter) == 1) {

                    connection = nodes.length / 2;
                    continue;
                }

                connection--;


            }
        }


    }

    public void printData(){
        for (int key: overlayMap.keySet()){
            System.out.print("Key: "+key+" =>");
            for(int i=0; i<overlayMap.get(key).size();++i){
                System.out.print(" "+overlayMap.get(key).get(i));
            }
            System.out.println();
        }
    }


    private int checkConnections(int[] counter){
        int prev = counter[0];
        for (int i=1;i<counter.length;++i){
            if (counter[i] == prev){
//                System.out.println("Counter value matched: "+prev);
                continue;
            }else {
                System.out.println("Counter Value mismatch..");
                return -1;
            }
        }
        return prev;
    }

    public static void main(String[] args){
        OverlaySetup obj = new OverlaySetup(10,4);
        obj.setupOverlay();
        obj.printData();
    }
}