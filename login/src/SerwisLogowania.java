import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class SerwisLogowania {
    public static void main(String[] args) {

        System.out.println("SerwisLogowania");

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Błąd ładowania pliku konfiguracyjnego");
            waitForUserInput();
        }

        int loginPort = Integer.parseInt(properties.getProperty("login.service.port"));

        try (ServerSocket serverSocket = new ServerSocket(loginPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Logowanie(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("500;Login Service ERROR. " + e.getMessage());
            waitForUserInput();
        }

    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
}