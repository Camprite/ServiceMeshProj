import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Agent {
    private static final String[][] ServiceRepository={{"apigateway"},{"login","rejestracja"},{"pliki","posty"}};
    private static ArrayList<MicroServiceData> RunningServices=new ArrayList<>();

    protected static String agentType;
    protected static Socket clientSocketLast;
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("[AGENT PROCESS]");

        String agentPort = args[0];
        String agentIP = args[1];
        String managerPort = args[2];
        String managerIP = args[3];
        agentType = args[4];
//
//        for (String s:args
//             ) {
//            System.out.println(s);
//        }





// NEW THREAD
            Runnable myThread = () ->
            {
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
                            "agentType:"+agentType+";" +
                            "agent_network_address:" + agentPort + ";" +
                            "service_repository:"+listInString(ServiceRepository[Integer.parseInt(agentType)])
                    );
                    System.out.println("Initiation request has been sended to Manager");
                    System.out.println(input.readLine());
                //                    REQUEST SEGMENT
                while (true) {
                String request = input.readLine();

                try {
                        if(request != null) {
                        System.out.println("[REQUEST FROM MANAGER]" + request);
                        String[] ManagerData = request.split(";");
                        String requestType = ManagerData[0].split(":")[1];
                        if(ManagerData[ManagerData.length-1].split(":")[0].compareTo("Status")==0) {
                            PrintWriter outputFromMicroservice = new PrintWriter(clientSocketLast.getOutputStream(), true);
                            String response=request;
                            ManagerData = response.split(";");
                            StringBuilder sb= new StringBuilder(ManagerData[0]);
                            sb.append(";"+ManagerData[1]);
                            for (int i=3;i<ManagerData.length;i++){
                                sb.append(";"+ManagerData[i]);
                            }
                            outputFromMicroservice.println(sb.toString());
                            outputFromMicroservice.flush();
                            continue;
                        }
                        if (requestType.equals("execution_request")) {
                            String serviceName = ManagerData[3].split(":")[1];
                            if(serviceName.compareTo("logowanie")==0){
                                serviceName="login";
                            }
                            System.out.println("[REQUEST]" + "execution request has been detected");
                            String portNumber = ManagerData[5].split(":")[1];
                            try {
                                boolean foundService=false;
                                for (String aService:ServiceRepository[Integer.parseInt(agentType)]){
                                    if(aService.compareTo(serviceName)==0){
                                        foundService=true;
                                        String servicePath = System.getProperty("user.dir") + "\\"+serviceName+".jar";
                                        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", servicePath, agentIP, agentPort,portNumber,agentIP);
                                        Process process = processBuilder.start();
                                        RunningServices.add(new MicroServiceData(process,portNumber,agentIP,serviceName));
                                        break;
                                    }
                                }
                                if(!foundService){
                                    throw new Exception("Wrong service name!!!");
                                }
                            }catch (Exception e) {
                                System.out.println("[AGENT FAILED] Processbuilder failed");
                                System.out.println(e.getMessage());
                            }


                        }
                        if (requestType.equals("microserviceadress_request")) {
                            String serviceName = ManagerData[3].split(":")[1];
                            if(serviceName.compareTo("logowanie")==0){
                                serviceName="login";
                            }
                            boolean found=false;
                            for(MicroServiceData msd:RunningServices){
                                if(msd.ServiceName.compareTo(serviceName)==0){
                                    output.println(request+";ServiceIP:"+msd.IpNumber+";Port:"+msd.PortNumber+";Status:200");
                                    output.flush();
                                    found=true;
                                    break;
                                }
                            }
                            if (!found){
                                output.println(request+";ServiceIP:"+0+";Port:"+0+";Status:400");
                                output.flush();
                            }

                        }
                    }
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
                } catch (IOException e) {
                    System.err.println("Błąd połączenia z Managerem." + e.getMessage());

                }
            };//THREAD END


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
                    clientSocketLast=clientSocket;
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

    static class MicroserviceThread  implements Runnable {
        private final Socket microserviceSocket;
        public MicroserviceThread(Socket clientSocket) {
            this.microserviceSocket = clientSocket;
        }

        @Override
        public void run() {

            try (BufferedReader inputFromMicroservice = new BufferedReader(new InputStreamReader(microserviceSocket.getInputStream()));
                 PrintWriter outputFromMicroservice = new PrintWriter(microserviceSocket.getOutputStream(), true)) {
                while (true) {
                        String request = inputFromMicroservice.readLine();
                    if(request != null){
                        System.out.println("[REQUEST]: " + request);
                        try {
                        String[] ManagerData = request.split(";");
                        String[] RequestType = ManagerData[0].split(":");
                        if (RequestType[1].equals("execution_request")) {
                            System.out.println("execution request has been detected");
                        }
                        if (RequestType[1].equals("microserviceadress_request")) {
                            try (Socket socket = new Socket("localhost", 9100);
                                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true)){
                                    StringBuilder sb= new StringBuilder(ManagerData[0]);
                                    sb.append(";"+ManagerData[1]);
                                    sb.append(";AgentType:"+agentType);
                                    for (int i=2;i<ManagerData.length;i++){
                                        sb.append(";"+ManagerData[i]);
                                    }
                                    request=sb.toString();
                                    output.println(request);
                                    output.flush();
                            }
                            catch (Exception e){
                                System.out.println("connectToManagerProblem");
                            }
                      }


                    } catch (Exception e) {
                        System.out.println("Nie mogę podzielić requesta");
//                        e.printStackTrace();
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
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }

    static class MicroServiceData{
        protected Process ServiceProcess;
        protected String PortNumber;
        protected String IpNumber;
        protected String ServiceName;

        public MicroServiceData(Process process,String port,String ip,String name){
            ServiceProcess=process;
            PortNumber=port;
            IpNumber=ip;
            ServiceName=name;
        }
    }

    public static String listInString(String[] listOfServices){
        StringBuilder result=new StringBuilder();
        for (String s:listOfServices){
            result.append(s).append(" ");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}




