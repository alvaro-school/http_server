import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class App {
    private final static int PORT = 8080;
    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            Log.log("Listening on " + PORT + "...");
            for (;;) {
                Socket s = ss.accept();
                Log.log("Client: " + s.getInetAddress().getHostAddress());
                new Handler(s).start();
            }
        } catch (IOException e) {
            Log.err(e.getMessage());
        }
    }
}
