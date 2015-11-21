import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {
    private final String address;
    private final int port;
    private final BufferedReader in;
    private final BufferedWriter out;
    private Socket socket;
    private static Scanner keyboard = new Scanner(System.in);

    public final static String START_GAME = "startGame";
    public final static String CONFIRM_REGIST = "confirmRegistration";
    public final static String MY_NEXT_MOVE = "move";
    public final static String YOU_MOVE = "nextMove";
    public final static String PLAYER_MOVED = "playerMoved";
    public final static String CONFIRM_START_GAME = "confirmStartGame";
    public final static String MOVE_OK = "moveOK";
    public final static String GAME_OVER = "gameOver";
    public final static String FAILED_REGISTRATION = "failedRegistration";
    public final static String ILLEGAL_MOVE = "illegalMove";
    public final static String REGIST = "registerTeam";
    public static final String CONFIRM_GAME_OVER = "gameOverConfirm";
    public static final String SHUTDOWN = "shutdown";

    public static int LVL;
    public static int WIDTH;
    public static int HEIGHT;
    public static Point current = new Point(0, 0);
    public static boolean[][] holes;
    public static boolean first = true;
    public static boolean iAmFirst = false;
    private AstarSearchAlgo algo;


    public Client(final String address, final int port) throws IOException {
        this.address = address;
        this.port = port;
        socket = new Socket(address, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private String readMsg() throws IOException {
        return in.readLine();
    }

    public void play() {
        boolean gameOn = true;
        try {
            sendMsg(REGIST + " " + getTeamName());
            while (gameOn) {
                final String nextMessage = readMsg();
                gameOn = handleGameStatus(nextMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTeamName() {
        return "HalfByte";
    }

    protected boolean handleGameStatus(final String nextMessage)
            throws IOException {
        System.out.println("get message " + nextMessage);

        final String[] args = nextMessage.split(" ");
        switch (args[0]) {
            case START_GAME:
                System.out.println("Start the game");
                LVL = Integer.parseInt(args[1]);
                WIDTH = Integer.parseInt(args[2]);
                HEIGHT = Integer.parseInt(args[3]);
                current.h = Integer.parseInt(args[5]);
                current.w = Integer.parseInt(args[4]);
                holes = new boolean[HEIGHT][WIDTH];
                first = true;
                iAmFirst = false;
                int traps = Integer.parseInt(args[6]);
                for (int i = 0; i < traps; i++) {
                    holes[Integer.parseInt(args[8 + i * 2])][Integer.parseInt(args[7 + i * 2])] = true;
                }
                algo = new AstarSearchAlgo(HEIGHT, WIDTH, holes);
                sendMsg(CONFIRM_START_GAME);
                break;
            case YOU_MOVE:
                String nextMove = null;
                if (first) {
                    first = false;
                    iAmFirst = true;
                    algo.initEndPoint(iAmFirst);
                    nextMove = algo.getNextMove(current, current.h, current.w, iAmFirst);
                }
                if (nextMove == null) {
                    nextMove = algo.getNextMove(current, current.h, current.w, false);
                }
                System.out.println(nextMove);
                sendMsg(MY_NEXT_MOVE + " " + nextMove);
                break;
            case PLAYER_MOVED:
                int newH = Integer.parseInt(args[2]);
                int newW = Integer.parseInt(args[1]);
                if (first) {
                    first = false;
                    iAmFirst = false;
                    algo.initEndPoint(iAmFirst);
                } else {
                    algo.deletePoint(newH, newW);
                }
                setArgument(args);
                current.h = newH;
                current.w = newW;
                break;
            case MOVE_OK:
                System.out.println("Good to know that I moved OK");
                break;
            case ILLEGAL_MOVE:
                System.out.println("Ups...");
                break;
            case CONFIRM_START_GAME:
                System.out.println("Game started");
                break;
            case CONFIRM_REGIST:
                System.out.println("Team registered");
                break;
            case FAILED_REGISTRATION:
                System.out.println("Now what???");
                break;
            case GAME_OVER:
                System.out.println("GAME OVER...");
                if ("0".equals(args[1])) {
                    System.out.println("You lost!");
                } else if ("1".equals(args[1])) {
                    System.out.println("You win!");
                }
                sendMsg(CONFIRM_GAME_OVER);
                break;
            case SHUTDOWN:
                System.out.println("Shut down...");
                return false;
            default:
                System.out.println("Unknown command ");
        }
        return true;
    }

    private void setArgument(String[] args) {
        System.out.println("Player moved to " + args[1] + " " + args[2]);
    }

    private String getNextMove() {
        System.out.print("Enter your next move as x y");
        final int x = keyboard.nextInt();
        final int y = keyboard.nextInt();
        return "" + x + " " + y;
    }

    protected void sendMsg(final String message) throws IOException {
        System.out.println("send " + message);
        out.write(message);
        out.write("\r\n");
        out.flush();
    }


    public static void main(final String... args)
            throws IOException, InterruptedException {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Client client = new Client(args[0], Integer.valueOf(args[1]));
                    client.play();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(r).run();
    }

    static class Point {
        int h, w;

        Point(int h, int w) {
            this.h = h;
            this.w = w;
        }

        boolean equals(Point a) {
            return (h == a.h) && (w == a.w);
        }

        static Point min(Point a, Point b) {
            return a.h == b.h ? (a.w < b.w ? a : b) : (a.h < b.h ? a : b);
        }

        static Point max(Point a, Point b) {
            return a.equals(min(a, b)) ? b : a;
        }

        public static Integer toInteger(Point a) {
            return a.h * HEIGHT + a.w;
        }
    }

//    class Registry {
//        Map<Integer, List<Point>> used_edges = new HashMap<>();
//        void add(Point a, Point b) {
//            List<Point> points = used_edges.get(Point.toInteger(Point.min(a, b)));
//            if (points == null)
//                points = new ArrayList<>();
//            points.add(Point.max(a, b));
//        }
//        boolean has(Point a, Point b) {
//            List<Point> points = used_edges.get(Point.toInteger(Point.min(a, b)));
//            return points != null && points.contains(Point.max(a, b));
//        }
//    }
}
