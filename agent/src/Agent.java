import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Agent {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("[AGENT PROCESS]");

        String agentPort = args[0];
        String agentIP = args[1];
        String managerPort = args[2];
        String managerIP = args[3];
        String agentType = args[4];
//
//        for (String s:args
//             ) {
//            System.out.println(s);
//        }

        try (Socket socket = new Socket(managerIP, Integer.parseInt(managerPort));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {



//            --------------------------------------Register to Manager request--------------------------------------
//            type: initiation_request
//            message_id: [integer]
//            agent_network_address: [IPv6]
//            service_repository: [service name list]
            output.println("type:initiation_request;" +
                    "message_id:"+agentType+";" +
                    "agent_network_address:" + agentPort + ";" +
                    "service_repository: none"
            );
            System.out.println("Initiation request has been sended to Manager");



// NEW THREAD
            Runnable myThread = () ->
            {
            //                    REQUEST SEGMENT
                while (true) {
                String request = null;
                try {
                    request = input.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("[REQUEST]" + request);
                try {
                    String[] ManagerData = request.split(";");
                    String[] requestType = ManagerData[0].split(":");
                    if (requestType[1].equals("execution_request")) {
                        System.out.println("[REQUEST]" + "execution request has been detected");
                        String servicePath = System.getProperty("user.dir") + "\\" + ManagerData[3].split(":")[1];
                        try {
                            ProcessBuilder ApiGateway = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", servicePath, agentIP, agentPort);
                            Process API = ApiGateway.start();
                        } catch (Exception e) {
                            System.out.println("[APIGATEWAY] Processbuilder failed");
                        }

                    }
                } finally {

                }
            }
            }
                ;
                // Instantiating Thread class by passing Runnable
                // reference to Thread constructor
                Thread run = new Thread(myThread);

                // Starting the thread
                run.start();







//    ---------------------------------------------Register to Manager END--------------------------------------------------
//            "type:execution_request;" +
//                    "message_id:0;" +
//                    "agent_network_address:none;" +
//                    "service_name:ApiGateway.jar;" +
//                    "service_instance_id:1;" +
//                    "socket_configuration:none;" +
//                    "plug_configuration:none");

            try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(agentPort))) {
                while (true) {
//                    CONNECTION SEGMENT
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Microservice connected on port: " + clientSocket.getLocalPort());
                    new Thread(new MicroserviceThread(clientSocket)).start();
                    }


            } catch (IOException e) {
                System.err.println("500;Login Service ERROR. " + e.getMessage());
                waitForUserInput();
            }

        } catch (IOException e) {
            System.err.println("Błąd połączenia z Managerem." + e.getMessage());
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

                while (true) {
                        String request = input.readLine();
                    if(request != null){
                        System.out.println("[REQUEST]: " + request);
                        try {
                        String[] ManagerData = request.split(";");
                        String[] RequestType = ManagerData[0].split(":");
                        if (RequestType[1].equals("execution_request")) {
                            System.out.println("execution request has been detected");
                        }


                    } catch (Exception e) {
                        System.out.println("Błędny typ requesta");
                        e.printStackTrace();
                    }}

                }

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
