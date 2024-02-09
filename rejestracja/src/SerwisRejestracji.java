import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class SerwisRejestracji {
    public static String portClient;
    public static String apiIp;
    public static String apiPort;
    public static String ipAgent;
    public static String portAgent;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Nieprawidłowa liczba argumentów.");
            System.out.println("Usage: java SerwisRejestracji <ipAgent> <portAgent> <port> <ip>");
            return;
        }

        System.out.println("Rejestracja");
         ipAgent = args[0];
         portAgent = args[1];
        String port = args[2];
        portClient = port;
        apiIp = args[3];
        apiPort = args[4];

        int registrationPort = Integer.parseInt(port);

        try (ServerSocket serverSocket = new ServerSocket(registrationPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Rejestracja(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Błąd " + e.getMessage());
        }

    }

    // Metoda do powiadomienia agenta
    public static void notifyAgent() {
        try (Socket socket = new Socket(ipAgent, Integer.parseInt(portAgent));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            output.println("finish_request"); // Wysyłamy informację "done" do agenta
        } catch (IOException e) {
            System.err.println("Błąd podczas wysyłania informacji do agenta: " + e.getMessage());
        }
        try (Socket socket = new Socket(apiIp, Integer.parseInt(apiPort));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            output.println("200: Pomyślnie zarejestrowano"); // Wysyłamy informację "" do api
        } catch (IOException e) {
            System.err.println("Błąd podczas wysyłania informacji do agenta: " + e.getMessage());
        }
    }
}
