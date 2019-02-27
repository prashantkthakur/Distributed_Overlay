package cs455.overlay.dijkstra;

import java.lang.*;

public class ShortestPath{

    private int[] dist;
    private int[] path;
    private int numNodes;

    public ShortestPath(int numNodes) {
        this.numNodes = numNodes;
        this.path = new int[numNodes];
        this.dist = new int[numNodes];
    }

    private int minDistance(int[] dist, boolean[] visitedSet){
        // Initialize min numNodes value
        int min = Integer.MAX_VALUE, minIdx=-1;

        for (int v = 0; v < numNodes; v++)
            if (!visitedSet[v] && dist[v] <= min)
            {
                min = dist[v];
                minIdx = v;
            }

        return minIdx;
    }

    public void printPath() {
        for (int val : path) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
    public void printDistance(){
        for(int val: dist){
            System.out.print(val+" ");
        }
        System.out.println();
    }
    public void computePath(int graph[][], int src){
	// Reference: http://geeksforgeeks.org/
	// int dist[] = new int[numNodes]; // The output array. dist[i] will hold
        // the shortest distance from src to i

        // visited[i] will true if vertex i is included in shortest
        // path tree or shortest distance from src to i is finalized
        boolean visited[] = new boolean[numNodes];

        // Initialize all distances as INFINITE and stpSet[] as false
        for (int i = 0; i < numNodes; i++)
        {
            dist[i] = Integer.MAX_VALUE;
            path[i] = -1;
            path[src] = src;
        }

        // Distance of source vertex from itself is always 0
        dist[src] = 0;

        // Find shortest path for all vertices
        for (int count = 0; count < numNodes-1; count++)
        {
            // Pick the minimum distance vertex from the set of vertices
            // not yet processed. u is always equal to src in first
            // iteration.
            int u = minDistance(dist, visited);

            // Mark the picked vertex as processed
            visited[u] = true;
            //path[u] = u;

            // Update dist value of the adjacent vertices of the
            // picked vertex.
            for (int v = 0; v < numNodes; v++)

                // Update dist[v] only if is not in visited, there is an
                // edge from u to v, and total weight of path from src to
                // v through u is smaller than current value of dist[v]
                if (!visited[v] && graph[u][v]!=0 &&
                        dist[u] != Integer.MAX_VALUE &&
                        dist[u]+graph[u][v] < dist[v]){
                    dist[v] = dist[u] + graph[u][v];
                    path[v] = u;
                }
        }

        // print the constructed distance array
//        printSolution(dist, numNodes);
    }
    public int[] getDist(){
        return dist;
    }
    public int[] getPath(){
        return path;
    }
    // Driver method
    public static void main (String[] args)
    {
        int graph[][] = new int[][]{
                {0, 4, 3, 0, 5, 4, 0, 0, 0, 0},
                {4, 0, 5, 1, 10, 0, 0, 0, 0, 0},
                {3, 5, 0, 0, 0, 1, 5, 0, 0, 0},
                {0, 1, 0, 0, 2, 0, 0, 0, 1, 10},
                {5, 10, 0, 2, 0, 0, 0, 0, 0, 5},
                {4, 0, 1, 0, 0, 0, 10, 10, 0, 0},
                {0, 0, 5, 0, 0, 10, 0, 10, 8, 0},
                {0, 0, 0, 0, 0, 10, 10, 0, 1, 5},
                {0, 0, 0, 1, 0, 0, 8, 1, 0, 9},
                {0, 0, 0, 10, 5, 0, 0, 5, 9, 0}
        };
        ShortestPath sp = new ShortestPath(graph.length);
        sp.computePath(graph, 9);
        sp.printPath();
        sp.printDistance();
    }
}
