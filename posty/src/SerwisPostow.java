import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SerwisPostow {
    public static String portClient;
    public static String apiIp;
    public static String apiPort;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Nieprawidłowa liczba argumentów.");
            System.out.println("Usage: java SerwisPostow <ipAgent> <portAgent> <port> <ip>");
            return;
        }

        System.out.println("SerwisPostow");
        String ipAgent = args[0];
        String portAgent = args[1];
        String port = args[2];
        portClient = port;
        apiIp = args[3];
        apiPort = args[4];

        int postPort = Integer.parseInt(port);

        try (ServerSocket serverSocket = new ServerSocket(postPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Posty(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Błąd " + e.getMessage());
        }


    }

    // Metoda do powiadomienia agenta
    public static void notifyAgent() {
        try (Socket socket = new Socket(apiIp, Integer.parseInt(apiPort));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            output.println("finish_request");
        } catch (IOException e) {
            System.err.println("Błąd podczas wysyłania informacji do agenta: " + e.getMessage());
        }
    }
}
