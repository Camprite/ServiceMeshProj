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
        String agentType = args[4];

        for (String s:args
             ) {
            System.out.println(s);
        }

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
            System.out.println("wysłano request");
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

                    String request = input.readLine();
                    System.out.println(request);
                    try {
                        String[] ManagerData = request.split(";");
                        String[] requestType = ManagerData[0].split(":");
                        if(requestType[1].equals("execution_request")){
                            System.out.println("execution request has been detected");
                            String servicePath =  System.getProperty("user.dir") + "\\" + ManagerData[3].split(":")[1];
                            ProcessBuilder ApiGateway = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  servicePath);
                            Process API = ApiGateway.start();

                        }
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Microservice connected on port: " + clientSocket.getLocalPort());
                        new Thread(new MicroserviceThread(clientSocket)).start();

                    }catch (Exception e){
                        System.out.println("wyrzucilo wyjątek");
                    }

                }
            } catch (IOException e) {
                System.err.println("500;Login Service ERROR. " + e.getMessage());
                waitForUserInput();
            }




//                while (true){
//
//                }



            //            output.print("TRSTTAS");
//            int i = 0;
//            while (i<10){
//                Thread.sleep(1000);
//                output.println(" ServiceType: " +agentPort);
//                System.out.printf(" wysylam ");
//                i++;
//            }
//            Thread.sleep(3000);

        } catch (IOException e) {
            System.err.println("Błąd połączenia z Managerem." + e.getMessage());
            waitForUserInput();
        }
//        if(ServiceNameList.equals("0")) {
//            System.out.println("true");
//            String MicroservicePath = System.getProperty("user.dir") + "\\ApiGateway.jar";
//            ProcessBuilder pbAPIGatewayAgent = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", MicroservicePath);
//            pbAPIGatewayAgent.start();
//        }







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
                    System.out.println(request);
                    try {
                        String[] ManagerData = request.split(";");
                        String[] RequestType = ManagerData[0].split(":");
                        if (RequestType[1].equals("execution_request")) {
                            System.out.println("execution request has been detected");
                        }


                    } catch (Exception e) {
                        System.out.println("Błędny typ requesta");
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
