import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SerwisLogowania {
    public static String portClient;
    public static String apiIp;
    public static String apiPort;

    public static String ipAgent;
    public static String portAgent;
    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            System.out.println("Nieprawidłowa liczba argumentów.");
            System.out.println("Usage: java SerwisLogowania <ipAgent> <portAgent> <port> <ip>");
            return;
        }

        System.out.println("SerwisLogowania");
         ipAgent = args[0];
         portAgent = args[1];
        String port = args[2];
        portClient = port;
        apiIp = args[3];
        apiPort = args[4];

        int loginPort = Integer.parseInt(port);

        try (ServerSocket serverSocket = new ServerSocket(loginPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("NEW CLIENT");
                new Thread(new Logowanie(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("500;Login Service ERROR. " + e.getMessage());
            return;
        }
    }

    public static void notifyAgent() {
        try (Socket socket = new Socket(ipAgent, Integer.parseInt(portAgent));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            output.println("finish_request");
        } catch (IOException e) {
            System.err.println("Błąd podczas wysyłania informacji do agenta: " + e.getMessage());
        }
    }
}
