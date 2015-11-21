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
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j] = new Node(i + "," + j, Math.sqrt(Math.pow(nodeMatrix.length - i - 1, 2) + Math.pow(nodeMatrix[0].length - j - 1, 2)), i, j);
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
        AstarSearchAlgo algo = new AstarSearchAlgo(300, 600, new boolean[300][600]);
        System.out.println("sdfds");
    }

    public void initEndPoint(boolean isIAmFirst) {
        if (isIAmFirst) {
            endPointX = 0;
        } else {
            endPointX = nodeMatrix[0].length;
        }
        endPointY = nodeMatrix.length / 2;
    }

    public String getNextMove(int moveX, int moveY, boolean isFirstMove) {
        if (endPointX == null) {
            initEndPoint(false);
        }
        Node currentNode = null;
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                if (i == moveX && j == moveY) {
                    currentNode = nodeMatrix[i][j];
                }
            }
        }
        if (!isFirstMove) {
            deleteEnemyEdge(this.currentX, this.currentY, moveX, moveY, nodeMatrix);
        }
        this.currentX = moveX;
        this.currentY = moveY;
        AstarSearch(currentNode, nodeMatrix[endPointX][endPointY]);
        List<Node> path = printPath(nodeMatrix[endPointX][endPointY]);
        if (path.size() > 0) {
            return "" + path.get(1).y + " " + path.get(1).x;
        } else {
            //TODO generate some;
            final int x = keyboard.nextInt();
            final int y = keyboard.nextInt();
            return "" + x + " " + y;
        }
    }

    private static void deleteEnemyEdge(int xWas, int yWas, int xEnemy, int yEnemy, Node[][] nodeMatrix) {
        List<Edge> edges = nodeMatrix[xWas][yWas].adjacencies;
        List<Edge> edgesCopy = Lists.newArrayList(nodeMatrix[xWas][yWas].adjacencies);
        for (Edge edge : edgesCopy) {
            if (edge.target.x == xEnemy && edge.target.y == yEnemy) {
                edges.remove(edge);
            }
        }
        nodeMatrix[xWas][yWas].adjacencies = edges;
    }

    private List<Edge> getAdjForNode(int i, int j, Node[][] nodeMatrix) {
        List<Edge> edges = new ArrayList<>(9);

        for (int i1 = i - 1; i1 <= i + 1; i1++) {
            for (int j1 = j - 1; j1 <= j + 1; j1++) {
                if (i == i1 && j == j1) {
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
    public List<Edge> adjacencies;
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