import com.google.common.collect.Lists;

import java.util.*;

public class AstarSearchAlgo {

    private static Node[][] nodeMatrix;

    public AstarSearchAlgo(int xsize, int ysize) {
        nodeMatrix = new Node[xsize][ysize];
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j] = new Node(i + "," + j, Math.sqrt(Math.pow(nodeMatrix.length - i - 1, 2) + Math.pow(nodeMatrix[0].length - j - 1, 2)), i, j);
            }
        }
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j].adjacencies = getAdjForNode(i, j, nodeMatrix);
            }
        }
    }

    public Node getNextMove(int currX, int currY) {
        Node currentNode = null;
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                if (i == currX && j == currY) {
                    currentNode = nodeMatrix[i][j];
                }
            }
        }
        //TODO method for delete
        int xWas = 0;
        int yWas = 0;
        int xEnemy = 0;
        int yEnemy = 1;
        deleteEnemyEdge(xWas, yWas, xEnemy, yEnemy, nodeMatrix);
        AstarSearch(currentNode, nodeMatrix[nodeMatrix.length - 1][nodeMatrix.length - 1]);
        List<Node> path = printPath(nodeMatrix[nodeMatrix.length - 1][nodeMatrix.length - 1]);
        if (path.size() > 0) {
            return path.get(1);
        } else {
            //TODO generate some;
        }
        return new Node("", 0, 0, 0);
    }

    //h scores is the stright-line distance from the current city to Bucharest
    public static void main(String[] args) {

    }

    private static void deleteEnemyEdge(int xWas, int yWas, int xEnemy, int yEnemy, Node[][] nodeMatrix) {
        //TODO
        ArrayList<Edge> edges = Lists.newArrayList(nodeMatrix[xWas][yWas].adjacencies);
        ArrayList<Edge> edgesCopy = Lists.newArrayList(nodeMatrix[xWas][yWas].adjacencies);
        for (Edge edge : edgesCopy) {
            if (edge.target.x == xEnemy && edge.target.y == yEnemy) {
                edges.remove(edge);
            }
        }
        nodeMatrix[xWas][yWas].adjacencies = edges.toArray(new Edge[edges.size()]);
    }

    private static Edge[] getAdjForNode(int i, int j, Node[][] nodeMatrix) {
        List<Edge> edges = new ArrayList<>();

        for (int i1 = i - 1; i1 <= i + 1; i1++) {
            for (int j1 = j - 1; j1 <= j + 1; j1++) {
                if (i == i1 && j == j1) {
                    continue;
                }
                if (isErrorTarget(i1, j1)) {
                    continue;
                }
                if (i1 >= 0 && i1 < nodeMatrix.length && j1 >= 0 && j1 < nodeMatrix[0].length) {
                    edges.add(new Edge(nodeMatrix[i1][j1], calculateWeight(i, j, nodeMatrix.length, nodeMatrix[i].length)));
                }
            }
        }
        Edge[] edgesArr = new Edge[edges.size()];
        return edges.toArray(edgesArr);
    }

    private static boolean isErrorTarget(int i1, int j1) {
        //TODO
        return false;
    }

    private static double calculateWeight(int i, int j, int imax, int jmax) {
        if (i == 0 || j == 0 || i == imax - 1 || j == jmax - 1) {
            return 0.0001;
        }
        if (j / 2 <= imax / 2) {
            return jmax - j;
        } else {
            return j + 1;
        }
    }

    public static List<Node> printPath(Node target) {
        List<Node> path = new ArrayList<Node>();
        for (Node node = target; node != null; node = node.parent) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }

    public static void AstarSearch(Node source, Node goal) {
        Set<Node> explored = new HashSet<Node>();
        PriorityQueue<Node> queue = new PriorityQueue<Node>(20,
                new Comparator<Node>() {
                    //override compare method
                    public int compare(Node i, Node j) {
                        if (i.f_scores > j.f_scores) {
                            return 1;
                        } else if (i.f_scores < j.f_scores) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }
        );
        //cost from start
        source.g_scores = 0;
        queue.add(source);
        boolean found = false;
        while ((!queue.isEmpty()) && (!found)) {
            //the node in having the lowest f_score value
            Node current = queue.poll();
            explored.add(current);
            //goal found
            if (current.value.equals(goal.value)) {
                found = true;
            }
            //check every child of current node
            for (Edge e : current.adjacencies) {
                Node child = e.target;
                double cost = e.cost;
                double temp_g_scores = current.g_scores + cost;
                double temp_f_scores = temp_g_scores + child.h_scores;
                                /*if child node has been evaluated and 
                                the newer f_score is higher, skip*/
                if ((explored.contains(child)) &&
                        (temp_f_scores >= child.f_scores)) {
                    continue;
                }
                                /*else if child node is not in queue or 
                                newer f_score is lower*/
                else if ((!queue.contains(child)) ||
                        (temp_f_scores < child.f_scores)) {
                    child.parent = current;
                    child.g_scores = temp_g_scores;
                    child.f_scores = temp_f_scores;
                    if (queue.contains(child)) {
                        queue.remove(child);
                    }
                    queue.add(child);
                }
            }
        }
    }
}

class Node {
    public final String value;
    public double g_scores;
    public int x;
    public int y;
    public final double h_scores;
    public double f_scores = 0;
    public Edge[] adjacencies;
    public Node parent;


    public Node(String value, double h_scores, int x, int y) {
        this.value = value;
        this.h_scores = h_scores;
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return value;
    }
}

class Edge {
    public final double cost;
    public final Node target;

    public Edge(Node targetNode, double costVal) {
        target = targetNode;
        cost = costVal;
    }
}