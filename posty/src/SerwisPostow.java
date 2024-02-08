import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class SerwisPostow {
    public static String portClient;
    public static ArrayList<Requests> toSend = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("SerwisPostow");
        String ipAgent = args[0];
        String portAgent = args[1];
        String port = args[2];
        portClient=port;
        String ip = args[3];
        System.out.println("PORT: " + port);
        System.out.println("IP: " + ip);

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