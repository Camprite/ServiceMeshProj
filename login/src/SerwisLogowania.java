import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class SerwisLogowania {
    public static PrintWriter outputAgent;
    public static String portClient;

    public static void main(String[] args) throws Exception {

        if(args[0] == null || args[1] == null){
            waitForUserInput();
            throw new Exception("Brak zdefioniowanych portów");
        }
        System.out.println("SerwisLogowania");
        String portAgent = args[0];
        String ipAgent = args[1];
        String port = args[2];
        portClient=port;
        String ip = args[3];
        System.out.println("PORT: " + port);
        System.out.println("IP: " + ip);
        Runnable myThread = () ->
        {
            try {
                Socket socket = new Socket(ipAgent, Integer.parseInt(portAgent));
                outputAgent = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String request = input.readLine();
                    try {
                    if(request != null) {
                        if(request.equals("KILL_ALL")){
                            System.exit(0);
                        }
                    }}catch(Exception ignore){}
                }
            } catch (Exception ignore){}};

        Thread run = new Thread(myThread);

        run.start();

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Błąd ładowania pliku konfiguracyjnego");
            waitForUserInput();
        }

//        int loginPort = Integer.parseInt(properties.getProperty("login.service.port")); CHANGED
        int loginPort = Integer.parseInt(port);

        try (ServerSocket serverSocket = new ServerSocket(loginPort)) {
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

