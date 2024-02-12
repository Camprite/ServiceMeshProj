import java.io.*;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.PrintWriter;
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
        // Pobierz argumenty z wiersza poleceń
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
        String ipAgent = args[0];
        int portAgent = Integer.parseInt(args[1]);
        int Port = Integer.parseInt(args[2]);
        String ip = args[3];

        try (ServerSocket serverSocket = new ServerSocket(registrationPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Rejestracja(clientSocket)).start();
                try {
                    // Nawiąż połączenie z ApiGateway
                    try (Socket socket = new Socket(ip, Port)) {
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                        // Wyślij wiadomość do ApiGateway
                        output.println("Udana rejestracja!");
                    }
                } catch (IOException e) {
                    System.err.println("Błąd " + e.getMessage());
                }

            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // Metoda do powiadomienia agenta
    public static void notifyAgent() {
        try (Socket socket = new Socket(ipAgent, Integer.parseInt(String.valueOf(portAgent)));
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
            System.err.println("Błąd podczas komunikacji z ApiGateway: " + e.getMessage());
        }
    }
}
