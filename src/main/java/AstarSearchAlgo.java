import com.google.common.base.Joiner;
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

    private int sprev1X;
    private int sprev1Y;

    private int sprev2X;
    private int sprev2Y;

    private int sprev3X;
    private int sprev3Y;

    private int sprev4X;
    private int sprev4Y;

    private int[] p1sprevsArr;

    private int[] p2sprevsArr;

    private int myIndex = 0;
    private boolean isIAmFirst;


    public AstarSearchAlgo(int xsize, int ysize, boolean[][] holes, int ensprev1X, int ensprev1Y, int ensprev2X, int ensprev2Y, int ensprev3X, int ensprev3Y, int ensprev4X, int ensprev4Y) {
        nodeMatrix = new Node[xsize][ysize];
        int code = 0;
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j] = new Node(code++, Math.sqrt(Math.pow(nodeMatrix.length - i - 1, 2) + Math.pow(nodeMatrix[0].length - j - 1, 2)), i, j);
            }
        }
        this.holes = holes;
        Arrays.stream(nodeMatrix).parallel().flatMap(Arrays::stream).parallel().forEach(it -> {
            it.adjacencies = getAdjForNode(it.x, it.y, nodeMatrix);
        });
        updateSprevs(ensprev1X, ensprev1Y, ensprev2X, ensprev2Y, ensprev3X, ensprev3Y, ensprev4X, ensprev4Y);
    }

    public void updateSprevs(int ensprev1X, int ensprev1Y, int ensprev2X, int ensprev2Y, int ensprev3X, int ensprev3Y, int ensprev4X, int ensprev4Y) {
        this.sprev1X = ensprev1X;
        this.sprev1Y = ensprev1Y;

        this.sprev2X = ensprev2X;
        this.sprev2Y = ensprev2Y;

        this.sprev3X = ensprev3X;
        this.sprev3Y = ensprev3Y;

        this.sprev4X = ensprev4X;
        this.sprev4Y = ensprev4Y;

        this.p1sprevsArr = new int[]{ensprev1X, ensprev1Y, ensprev2X, ensprev2Y};
        this.p2sprevsArr = new int[]{ensprev3X, ensprev3Y, ensprev4X, ensprev4Y};
    }

    public static void main(String[] args) {
        AstarSearchAlgo algo = new AstarSearchAlgo(31, 61, new boolean[31][61], 10, 10, 11, 11, 12, 12, 13, 13);
        Client.Point current = new Client.Point(30, 60);
        while (true) {
            String nextMove = algo.getNextMove(current, keyboard.nextInt(), keyboard.nextInt(), false);
//            String nextMove = algo.getNextMove(current, current.h, current.w, false);
            System.out.println(nextMove);
            if (current.w == 0 || current.h == 0) {
                nextMove = algo.getNextMove(current, current.h, current.w, false);
                System.out.println(nextMove);
            }
//            System.out.println(algo.getNextMove(current, 74, 148, false));
        }
    }

    public void initEndPoint(boolean isIAmFirst) {
        this.isIAmFirst = isIAmFirst;
        if (this.isIAmFirst) {
            endPointY = nodeMatrix[0].length - 1;
        } else {
            endPointY = 0;
        }
        endPointX = (nodeMatrix.length - 1) / 2;
    }

    public String getNextMove(Client.Point currentPointClient, int moveX, int moveY, boolean isFirstMove) {
        if (endPointX == null) {
            initEndPoint(false);
        }
        moveMySprevs(moveX, moveY);
        Node currentNode = nodeMatrix[moveX][moveY];
        if (!isFirstMove) {
            deleteEdge(this.currentX, this.currentY, moveX, moveY, nodeMatrix);
            deleteEdge(moveX, moveY, this.currentX, this.currentY, nodeMatrix);
        }
        recalculateWeight(nodeMatrix);
        AstarSearch(currentNode, nodeMatrix[endPointX][endPointY]);
        Node nextStep = printPath(nodeMatrix[endPointX][endPointY], currentNode);
        for (int i = 0; i < nodeMatrix.length; i++) {
            for (int j = 0; j < nodeMatrix[i].length; j++) {
                nodeMatrix[i][j].parent = null;
                nodeMatrix[i][j].g_scores = 0;
            }
        }
        System.gc();
        if (nextStep != null) {
            deleteEdge(this.currentX, this.currentY, nextStep.x, nextStep.y, nodeMatrix);
            deleteEdge(nextStep.x, nextStep.y, this.currentX, this.currentY, nodeMatrix);
            nextStep.h_scores -= nextStep.h_scores / 5;
            this.currentX = nextStep.x;
            this.currentY = nextStep.y;
            currentPointClient.h = nextStep.x;
            currentPointClient.w = nextStep.y;
            return "" + nextStep.y + " " + nextStep.x;
        } else {
            System.out.println("Cannot resolve step :(");
            final int x = keyboard.nextInt();
            final int y = keyboard.nextInt();
            return "" + x + " " + y;
        }
    }

    private void moveMySprevs(int moveX, int moveY) {
        int[] mySprevs = null;
        if (isIAmFirst) {
            mySprevs = this.p1sprevsArr;
        } else {
            mySprevs = this.p2sprevsArr;
        }
        if (mySprevs[0] > endPointX + 1) {
            mySprevs[0]--;
        }
        if (mySprevs[0] < endPointX + 1) {
            mySprevs[0]++;
        }
        if (mySprevs[1] < endPointY) {
            mySprevs[1]++;
        }

        if (mySprevs[1] > endPointY) {
            mySprevs[1]--;
        }

        if (mySprevs[2] > endPointX - 1) {
            mySprevs[2]--;
        }
        if (mySprevs[2] < endPointX - 1) {
            mySprevs[2]++;
        }
        if (mySprevs[3] < endPointY) {
            mySprevs[3]++;
        }

        if (mySprevs[3] > endPointY) {
            mySprevs[3]--;
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

    public Node printPath(Node target, Node stopPoint) {
        List<Node> path = new ArrayList<Node>();
        for (Node node = target; node != null; node = node.parent) {
            path.add(node);
            if (node.x == stopPoint.x && node.y == stopPoint.y) {
                break;
            }
        }
        Collections.reverse(path);
//        int i = startPoint.x;
//        int j = startPoint.y;
//        for (int i1 = i - 1; i1 <= i + 1; i1++) {
//            for (int j1 = j - 1; j1 <= j + 1; j1++) {
//                if (i1 >= 0 && i1 < nodeMatrix.length && j1 >= 0 && j1 < nodeMatrix[0].length) {
//                    if (nodeMatrix[i1][j1].parent != null) {
//                        return nodeMatrix[i1][j1];
//                    }
//                }
//            }
//        }
        if (path.size() > 1) {
            return path.get(1);
        }
        return path.get(0);
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

    public void deletePoint(int newH, int newW) {
        deleteEdge(this.currentX, this.currentY, newH, newW, nodeMatrix);
        deleteEdge(newH, newW, this.currentX, this.currentY, nodeMatrix);
    }

    public String getSprevPositions() {
        Object[] arr = new Object[]{
                sprev1Y, sprev1X, sprev2Y, sprev2X
        };
        return Joiner.on(" ").join(arr);
    }
}

class Node {
    public final int value;
    public double g_scores;
    public int x;
    public int y;
    public double h_scores;
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
        return "" + value + ": " + x + "," + y;
    }
}

class Edge {
    public final double cost;
    public final Node target;

    public Edge(Node targetNode, double costVal) {
        target = targetNode;
        cost = costVal;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "target=" + target +
                '}';
    }
}