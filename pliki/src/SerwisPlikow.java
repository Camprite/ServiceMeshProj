import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

public class SerwisPlikow {
    public static String portClient;
    public static String apiIp;
    public static String apiPort;
    public static String ipAgent;
    public static String portAgent;
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Nieprawidłowa liczba argumentów.");
            System.out.println("Usage: java SerwisPlikow <ipAgent> <portAgent> <port> <ip>");
            return;
        }

        System.out.println("SerwisPlikow");
        String ipAgent = args[0];
        String portAgent = args[1];
        String port = args[2];
        portClient = port;
        apiIp = args[3];
        apiPort = args[4];

        int loginPort = Integer.parseInt(port);

        try (ServerSocket serverSocket = new ServerSocket(loginPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Pliki(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("500;File Service ERROR." + e.getMessage());
        }
    }
        // Tworzenie wątku do komunikacji z agentem
        public static void notifyAgent() {
            try (Socket socket = new Socket(ipAgent, Integer.parseInt(portAgent));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
                output.println("done"); // Wysyłamy informację "done" do agenta
            } catch (IOException e) {
                System.err.println("Błąd podczas wysyłania informacji do agenta: " + e.getMessage());
            }
        }

}
