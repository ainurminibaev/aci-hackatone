import com.google.common.collect.Lists;

import java.util.*;

public class AstarSearchAlgo {

    private static Scanner keyboard = new Scanner(System.in);
    private final boolean[][] holes;

    private Node[][] nodeMatrix;

    private int currentX;

    private int currentY;

    private Integer endPointX;

    private int endPointY;

    public AstarSearchAlgo(int xsize, int ysize, boolean[][] holes) {
        nodeMatrix = new Node[xsize][ysize];
        int code = 0;
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j] = new Node(code++, Math.sqrt(Math.pow(nodeMatrix.length - i - 1, 2) + Math.pow(nodeMatrix[0].length - j - 1, 2)), i, j);
            }
        }
        this.holes = holes;
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j].adjacencies = getAdjForNode(i, j, nodeMatrix);
            }
        }
        Arrays.stream(nodeMatrix).parallel().flatMap(Arrays::stream).parallel().forEach(it -> {
            it.adjacencies = getAdjForNode(it.x, it.y, nodeMatrix);
        });
    }

    public static void main(String[] args) {
        AstarSearchAlgo algo = new AstarSearchAlgo(5, 7, new boolean[5][7]);
        Client.Point current = new Client.Point(0, 0);
        while (true) {
            String nextMove = algo.getNextMove(current, keyboard.nextInt(), keyboard.nextInt(), false);
            System.out.println(nextMove);
//            System.out.println(algo.getNextMove(current, 74, 148, false));
        }
    }

    public void initEndPoint(boolean isIAmFirst) {
        if (isIAmFirst) {
            endPointY = nodeMatrix[0].length - 1;
        } else {
            endPointY = 0;
        }
        endPointX = nodeMatrix.length / 2;
    }

    public String getNextMove(Client.Point currentPointClient, int moveX, int moveY, boolean isFirstMove) {
        if (endPointX == null) {
            initEndPoint(false);
        }
        Node currentNode = null;
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                if (i == moveX && j == moveY) {
                    currentNode = nodeMatrix[i][j];
                    nodeMatrix[i][j].parent = null;
                    nodeMatrix[i][j].g_scores = 0;
                }
            }
        }
        if (!isFirstMove) {
            deleteEdge(this.currentX, this.currentY, moveX, moveY, nodeMatrix);
        }
        recalculateWeight(nodeMatrix);
        AstarSearch(currentNode, nodeMatrix[endPointX][endPointY]);
        Node nextStep = printPath(currentNode);
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j].parent = null;
                nodeMatrix[i][j].g_scores = 0;
            }
        }
        System.gc();
        if (nextStep != null) {
            deleteEdge(this.currentX, this.currentY, nextStep.x, nextStep.y, nodeMatrix);
            this.currentX = nextStep.x;
            this.currentY = nextStep.y;
            currentPointClient.h = nextStep.y;
            currentPointClient.w = nextStep.x;
            return "" + nextStep.y + " " + nextStep.x;
        } else {
            System.out.println("Cannot resolve step :(");
            final int x = keyboard.nextInt();
            final int y = keyboard.nextInt();
            return "" + x + " " + y;
        }
    }

    private void recalculateWeight(Node[][] nodeMatrix) {

    }

    private static void deleteEdge(int xWas, int yWas, int xEnemy, int yEnemy, Node[][] nodeMatrix) {
        List<Edge> edges = nodeMatrix[xWas][yWas].adjacencies;
        List<Edge> edgesCopy = Lists.newArrayList(nodeMatrix[xWas][yWas].adjacencies);
        for (int i = 0; i < edgesCopy.size(); i++) {
            if (edgesCopy.get(i).target.x == xEnemy && edgesCopy.get(i).target.y == yEnemy) {
                edges.remove(edgesCopy.get(i));
            }
        }
        nodeMatrix[xWas][yWas].adjacencies = edges;
    }

    private List<Edge> getAdjForNode(int i, int j, Node[][] nodeMatrix) {
        List<Edge> edges = new ArrayList<>(9);

        for (int i1 = i - 1; i1 <= i + 1; i1++) {
            for (int j1 = j - 1; j1 <= j + 1; j1++) {
                if (i == i1 && j == j1
                        || (i == i1 && i == 0)
                        || (j == j1 && j == 0)
                        || (i == i1 && i == nodeMatrix[0].length - 1)
                        || (j == j1 && j == nodeMatrix.length - 1)) {
                    continue;
                }
                if (i1 >= 0 && i1 < nodeMatrix.length && j1 >= 0 && j1 < nodeMatrix[0].length) {
                    if (isErrorTarget(i1, j1)) {
                        continue;
                    }
                    edges.add(new Edge(nodeMatrix[i1][j1], calculateWeight(i, j, nodeMatrix.length, nodeMatrix[i].length)));
                }
            }
        }
        return edges;
    }

    private boolean isErrorTarget(int i1, int j1) {
        return holes[i1][j1];
    }

    private static double calculateWeight(int i, int j, int imax, int jmax) {
        if (i == 0 || j == 0 || i == imax - 1 || j == jmax - 1 || i == 1 || j == 1 || i == imax - 2 || j == jmax - 2) {
            return 0.001;
        }
        if (j / 2 <= imax / 2) {
            return jmax - j;
        } else {
            return j + 1;
        }
    }

    public Node printPath(Node startPoint) {
//        List<Node> path = new ArrayList<Node>();
//        for (Node node = target; node != null; node = node.parent) {
//            path.add(node);
//        }
//        Collections.reverse(path);
        int i = startPoint.x;
        int j = startPoint.y;
        for (int i1 = i - 1; i1 <= i + 1; i1++) {
            for (int j1 = j - 1; j1 <= j + 1; j1++) {
                if (i1 >= 0 && i1 < nodeMatrix.length && j1 >= 0 && j1 < nodeMatrix[0].length) {
                    if (nodeMatrix[i1][j1].parent != null) {
                        return nodeMatrix[i1][j1];
                    }
                }
            }
        }
        return null;
    }

    public void AstarSearch(Node source, Node goal) {
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
            if (current.value == goal.value) {
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
        explored.clear();
        queue.clear();
    }
}

class Node {
    public final int value;
    public double g_scores;
    public int x;
    public int y;
    public final double h_scores;
    public double f_scores = 0;
    public List<Edge> adjacencies;
    public Node parent;


    public Node(int value, double h_scores, int x, int y) {
        this.value = value;
        this.h_scores = h_scores;
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "" + value;
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