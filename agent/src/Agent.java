import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Agent {
    private static final String[][] ServiceRepository={{"apigateway"},{"login","rejestracja"},{"pliki","posty"}};
    private static  ArrayList<String> AgentResponsible = new ArrayList<String>();
    private static ArrayList<MicroServiceData> RunningServices=new ArrayList<>();
    private static ArrayList<Socket> allSockets = new ArrayList<>();

    public static String agentPort;
    public static String agentIp;
    protected static String agentType;
    protected static String managerIp;
    protected static String managerPort;
    protected static Socket clientSocketLast;

    //    getting agent ip
    static {
        try {
            agentIp = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    };
    public static void main(String[] args) throws InterruptedException, IOException {
//        String[] args =
//        [0] = Manager IP
//        [1] = Manager Port
//        [2] = Agent Type
        managerIp = args[0];
        managerPort = args[1];
        agentType = args[2];
        System.out.println("[AGENT PROCESS]");

//        Define agent responsible;
        if(agentType == "0") {
            AgentResponsible.add("api"); //Adding responsible to Agent with 0 index
        }else if(agentType == "1") {
            AgentResponsible.add("login");
            AgentResponsible.add("rejestracja"); //Adding responsible to Agent with 1 index
        }else if(agentType == "2"){
            AgentResponsible.add("posty");
            AgentResponsible.add("pliki"); //Adding responsible to Agent with 2 index

        }

// NEW THREAD
            Runnable myThread = () ->
            {
                try (Socket socket = new Socket(managerIp, Integer.parseInt(managerPort));
                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

//            --------------------------------------Register to Manager request--------------------------------------
//            Type: initiation_request
//            Message_id: [integer]
//            Service_name: [service name list]
//            Agent_type: agentType
//            Line: "" //Tutaj ip i port agenta

                    String microservicesList = "";
                    for (String microservice : AgentResponsible
                         ) {
                        microservicesList = microservice + " " + microservicesList ;
                    }
                    output.println(new Requests("initiation_request",1+agentType,listInString(ServiceRepository[Integer.parseInt(agentType)]),agentType, agentIp+';'+agentPort+ ";" + microservicesList));
                    System.out.println("Initiation request has been sent to Manager");
                    System.out.print("Response: ");
                    System.out.println(input.readLine());

                //                    REQUEST SEGMENT
                while (true) {
                String request = input.readLine();

                try {
                    if(request != null) {
                        System.out.println("[REQUEST FROM MANAGER]: " + request);

                        boolean IsItStatus = request.split(";")[2].split(":")[0].compareTo("Status") == 0;
                        if(IsItStatus) {
                            PrintWriter outputFromMicroservice = new PrintWriter(clientSocketLast.getOutputStream(), true);
                            outputFromMicroservice.println(request);
                            outputFromMicroservice.flush();
                            continue;
                        }
                        Requests true_request = new Requests(request);
                        if (true_request.Type.equals("execution_request")) {
                            System.out.println("[REQUEST]: execution request has been detected");
                            String portNumber = true_request.Line;
                            try {
                                boolean foundService=false;
                                for (String aService:ServiceRepository[Integer.parseInt(agentType)]){
                                    if(aService.compareTo(true_request.Service_name)==0){
                                        foundService=true;
                                        String servicePath = System.getProperty("user.dir") + "\\"+true_request.Service_name+".jar";
                                        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", servicePath, agentIp, agentPort,portNumber,agentIp);
                                        Process process = processBuilder.start();
                                        RunningServices.add(new MicroServiceData(process,portNumber,agentIp,true_request.Service_name));
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
                        if (true_request.Type.equals("microserviceadress_request")) {
                            boolean found=false;
                            for(MicroServiceData msd:RunningServices){
                                if(msd.ServiceName.compareTo(true_request.Service_name)==0 && !msd.isBusy()){
                                    msd.toggleBusy();
                                    true_request.Line = msd.IpNumber+";"+msd.PortNumber;
                                    output.println(new Responses(true_request,"200"));
                                    output.flush();
                                    found=true;
                                    break;
                                }
                            }
                            if (!found){
                                output.println(new Responses(true_request,"400"));
                                output.flush();
                            }

                        }
                    }
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
                } catch (IOException e) {
                    System.err.println("Błąd połączenia z Managerem: " + e.getMessage());

                }
            };//THREAD END


                // Instantiating Thread class by passing Runnable
                // reference to Thread constructor
                Thread run = new Thread(myThread);

                // Starting the thread
                run.start();







//    ---------------------------------------------Register to Manager END--------------------------------------------------


            try (ServerSocket serverSocket = new ServerSocket(0)) {

                System.out.println("Agent server started on port: " + serverSocket.getLocalPort());
                agentPort = Integer.toString(serverSocket.getLocalPort());

                while (true) {
//                    CONNECTION SEGMENT
                        Socket clientSocket = serverSocket.accept();
                        String clientPort = String.valueOf(clientSocket.getLocalPort());
                        allSockets.add(clientSocket);
                        clientSocketLast = clientSocket;
                        System.out.println("Microservice connected on port: " + clientPort);
                        new Thread(new MicroserviceThread(clientSocket)).start();
                    }

            } catch (IOException e) {
                System.err.println("500;Login Service ERROR. " + e.getMessage());
            }

    }

    static class MicroserviceThread  implements Runnable {
        private final Socket microserviceSocket;
        public MicroserviceThread(Socket clientSocket) {
            this.microserviceSocket = clientSocket;
        }

        @Override
        public void run() {

            try (BufferedReader inputFromMicroservice = new BufferedReader(new InputStreamReader(microserviceSocket.getInputStream()))) {
                while (true) {
                    String request = inputFromMicroservice.readLine();
                    if(request != null){
                        System.out.println("[REQUEST]: " + request);
                        try {
                            boolean IsItStatus = request.split(";")[2].split(":")[0].compareTo("Status") == 0;
                            if(IsItStatus){
                                Responses response = new Responses(request);
                                String portNumber = response.Line;
                                for (MicroServiceData msd:RunningServices){
                                    if(msd.PortNumber.equals(portNumber)){
                                        msd.toggleBusy();
                                        break;
                                    }
                                }
                                continue;
                            }
                            Requests true_request = new Requests(request);
                            if(true_request.Type.equals("not_busy")){
                                for (MicroServiceData msd:RunningServices){
                                    if(msd.PortNumber.equals(true_request.Line)){
                                        msd.toggleBusy();
                                        break;
                                    }
                                }
                            }
                            if (true_request.Type.equals("execution_request")) {
                                System.out.println("execution request has been detected");
                            }
                            if (true_request.Type.equals("microserviceadress_request")) {
                                try (Socket socket = new Socket("localhost", Integer.parseInt(managerPort));
                                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true)){
//                                    true_request.Agent_type = agentType;
                                    output.println(true_request);
                                    output.flush();
                                }
                                catch (Exception e){
                                    System.out.println("connectToManagerProblem");
                                }
                          }
                        } catch (Exception e) {
                            System.out.println("Nie mogę podzielić requesta");
                        }
                    }
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

    static class MicroServiceData{
        protected Process ServiceProcess;
        protected String PortNumber;
        protected String IpNumber;
        protected String ServiceName;

        protected boolean IsBusy;

        public MicroServiceData(Process process,String port,String ip,String name){
            ServiceProcess=process;
            PortNumber=port;
            IpNumber=ip;
            ServiceName=name;
            IsBusy = false;
        }

        public boolean isBusy(){
            return IsBusy;
        }

        public void toggleBusy(){
            IsBusy = !IsBusy;
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




