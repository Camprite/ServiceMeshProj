import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class SerwisRejestracji {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Błąd w pliku konfiguracyjnym  " + e.getMessage());
            waitForUserInput();
        }

        int registrationPort = Integer.parseInt(properties.getProperty("registration.service.port"));

        try (ServerSocket serverSocket = new ServerSocket(registrationPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Rejestracja(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Błąd");
        }
    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
}