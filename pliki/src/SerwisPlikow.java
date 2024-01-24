import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SerwisPlikow {
    public static void main(String[] args) {
        System.out.println("SerwisPlikow");
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
            System.err.println("Błąd ładowania pliku konfiguracyjnego");
            return;
        }

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
}