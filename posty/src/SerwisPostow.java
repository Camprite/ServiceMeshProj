import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SerwisPostow {
    public static void main(String[] args) {
        System.out.println("SerwisPostow");
        String portAgent = args[0];
        String ipAgent = args[1];
        String port = args[2];
        String ip = args[3];
        System.out.println("PORT: " + port);
        System.out.println("IP: " + ip);

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Błąd pliku konfiguracyjnego." + e.getMessage());
            return;
        }

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
}