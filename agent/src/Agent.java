import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Agent {
    public static void main(String[] args) throws InterruptedException {

        String agentPort = args[0];
        String agentIP = args[1];
        String managerPort = args[2];
        String managerIP = args[3];
        for (String s:args
             ) {
            System.out.println(s);
        }
        Thread.sleep(2000);
//        String requestFromManager = args[4];
        try (Socket socket = new Socket(managerIP, Integer.parseInt(managerPort));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String requestFromManager = input.readLine();

            if(requestFromManager.length()>0){
                System.out.println("Resquest Given");
                String[] managerData = requestFromManager.split(";");
                if(managerData[0] == "Start"){
                    System.out.println("Startuje: "+ managerData[1]);
                    String ExecPath = System.getProperty("user.dir") + "\\" + managerData[1];
                    ProcessBuilder process = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  ExecPath , "9000", "localhost");
                    Process proc = process.start();
                    System.out.println(proc.isAlive());

                }
            }else {
                System.out.println("resquest not given");
            }
//            output.println(request);
            System.out.println("połączono");
                input.readLine();
        } catch (IOException e) {
            System.err.println("Błąd połączenia z Managerem." + e.getMessage());
//            return "Błąd połączenia z ApiGateway.";
        }
        waitForUserInput();

//        Connection to Maganger
//        Get request from Manager
//        Open specific service by response from Manager data
//        Return to maganger status of processes





    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
}
