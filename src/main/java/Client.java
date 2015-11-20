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
        System.out.print("Enter your Team Name:");
        return keyboard.nextLine();
    }

    protected boolean handleGameStatus(final String nextMessage)
            throws IOException {
        System.out.println("get message " + nextMessage);

        final String[] args = nextMessage.split(" ");

        switch (args[0]) {
            case START_GAME:
                System.out.println("Start the game");
                sendMsg(CONFIRM_START_GAME);
                break;
            case YOU_MOVE:
                sendMsg(MY_NEXT_MOVE + " " + getNextMove());
                break;
            case PLAYER_MOVED:
                setArgument(args);
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
}
