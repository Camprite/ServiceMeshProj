import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Agent {
    public static void main(String[] args) throws InterruptedException, IOException {

        String agentPort = args[0];
        String agentIP = args[1];
        String managerPort = args[2];
        String managerIP = args[3];
        String ServiceNameList = args[4];

        for (String s:args
             ) {
            System.out.println(s);
        }
//        Thread.sleep(2000);
        try (Socket socket = new Socket(managerIP, Integer.parseInt(managerPort));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            output.write("ServiceType: " +ServiceNameList);
            output.flush();
            System.out.println("połączono");
            Thread.sleep(3000);

        } catch (IOException e) {
            System.err.println("Błąd połączenia z Managerem." + e.getMessage());
            waitForUserInput();
        }
        if(ServiceNameList.equals("0")) {
            System.out.println("true");
            String MicroservicePath = System.getProperty("user.dir") + "\\ApiGateway.jar";
            ProcessBuilder pbAPIGatewayAgent = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", MicroservicePath);
            pbAPIGatewayAgent.start();
        }




        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(agentPort))) {
            Thread.sleep(1000);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Microservice connected on port: " + clientSocket.getLocalPort());
                new Thread(new MicroserviceThread(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("500;Login Service ERROR. " + e.getMessage());
            waitForUserInput();
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
    static class MicroserviceThread  implements Runnable {
        private final Socket microserviceSocket;
        public MicroserviceThread(Socket clientSocket) {
            this.microserviceSocket = clientSocket;
        }

        @Override
        public void run() {

            try (BufferedReader input = new BufferedReader(new InputStreamReader(microserviceSocket.getInputStream()));
                 PrintWriter output = new PrintWriter(microserviceSocket.getOutputStream(), true)) {

                String request = input.readLine();
                String[] userData = request.split(":");
                System.out.println(userData[0]);
                System.out.println(userData[1]);

            } catch (IOException e) {
                System.err.println("Błąd." + e.getMessage());
            } finally {
                try {
                    microserviceSocket.close();
                } catch (IOException e) {
                    System.err.println("Błąd socketa");
                }
            }

        }
    }

}
