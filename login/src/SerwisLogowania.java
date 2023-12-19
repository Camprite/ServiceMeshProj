import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class SerwisLogowania {
    public static void main(String[] args) throws Exception {

        if(args[0] == null || args[1] == null){
            waitForUserInput();
            throw new Exception("Brak zdefioniowanych portów");
        }
        System.out.println("SerwisLogowania");
        String port = args[0];
        String ip = args[1];
        System.out.println("PORT: " + port);
        System.out.println("IP: " + ip);
        System.out.println("Logowanie on port: " + args[2]);


        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Błąd ładowania pliku konfiguracyjnego");
            waitForUserInput();
        }

//        int loginPort = Integer.parseInt(properties.getProperty("login.service.port")); CHANGED
        int loginPort = Integer.parseInt(args[2]);

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("NEW CLIENT");
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

