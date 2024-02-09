import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class SerwisLogowania {
    public static String portClient;
    public static ArrayList<Requests> toSend = new ArrayList<>();
    public static void main(String[] args) throws Exception {

        if(args[0] == null || args[1] == null){
            waitForUserInput();
            throw new Exception("Brak zdefioniowanych portów");
        }
        System.out.println("SerwisLogowania");
        String ipAgent = args[0];
        String portAgent = args[1];
        String port = args[2];
        portClient=port;
        String ip = args[3];
        System.out.println("PORT: " + port);
        System.out.println("IP: " + ip);
        System.out.println("Logowanie on port: " + args[2]);

//LISTEN FOR TERMINATION REQUEST
        Runnable myThread = () ->
        {  try {
            Socket socket = null;
            while (socket == null){
                try {
                    socket = new Socket(ipAgent, Integer.parseInt(portAgent));
                } catch (Exception ignore){
                }
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String request = input.readLine();

                    if(request != null) {
                        if(request.equals("KILL_ALL")){
                            System.exit(0);
                        }
                    }
            }}catch(Exception ignore){}
           };
        Thread run = new Thread(myThread);
        run.start();
//SEND REQUESTS TO AGENT
        Runnable myThread2 = () ->
        {  try {
            Socket socket = null;
            while (socket == null){
                try {
                    socket = new Socket(ipAgent, Integer.parseInt(portAgent));
                } catch (Exception ignore){
                }
            }
            PrintWriter outputAgent = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                while (toSend.isEmpty()){
                    Thread.sleep(1000);
                }
                outputAgent.println(toSend.remove(0));
                outputAgent.flush();
            }}catch(Exception ignore){}
        };
        Thread run2 = new Thread(myThread2);
        run2.start();

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
        waitForUserInput();

    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
}

