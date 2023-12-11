import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

//  PRZYDATNE :)
//  netstat -ano | find "9000"
//  taskkill /F /PID your_PID
//


/*
jeśli przy otrzymaniu requesta nie ma w arrayliście połączeń zadanego typu np:
request: logowanie

arraylist Połączenia:{rejestracja,posty}
arraylist Agenci:{}

klasa połączenie = Typ Serwisu, obiekt klasy agent, ip, port

klasa agent = id Aganta, arraylista obsługiwanych serwisów, ip, port

utwórz nowy serwis logowanie i dodaj go do arraylisty Połączenia


Request from Manager to the agent to execute the instance i of service A
        type: execution_request
        message_id: n
        agent_network_address: NA_i
        service_name: A
        service_instance_id: i
        socket_configuration: [configuration of sockets]
        plug_configuration: [configuration of plugs]


 */

public class Manager {





    public static Socket Agent0;
    public static Socket Agent1;
    public static Socket Agent2;
//    static String RequestToCreateApiGatewayMicroservice = "type:execution_request \n" +
//            "message_id: Start Api Gateway Microservice \n" +
//            "agent_network_address:localhost:9010 \n" +
//            "service_name:\\ApiGateway.jar\n" +
//            "service_instance_id: 0\n" +
//            "socket_configuration: none\n" +
//            "plug_configuration: none\n";
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("[MANAGER]");
        String ManagerPort = "9100";
        String ManagerIP = "localhost";
        String AgentPath = System.getProperty("user.dir") + "\\agent.jar";



//          AGENTS TYPES  0 = APIGATEWAY AGENT, 1 = LOGIN + REGISTER AGENT, 2 = POSTS AND FILES AGENT
        ProcessBuilder pbAPIGatewayAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9010", "localhost", ManagerPort, ManagerIP, "0");
        ProcessBuilder pbLoginRegisterAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9020", "localhost", ManagerPort, ManagerIP, "1");
        ProcessBuilder pbPostsAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9030", "localhost", ManagerPort, ManagerIP, "2");

//Opening Agent processes
        try {
            Process ProcAPIGatewayAgent = pbAPIGatewayAgent.start();
            Process ProcLoginRegisterAgent = pbLoginRegisterAgent.start();
            Process ProcPostsAgent = pbPostsAgent.start();
            if(!ProcAPIGatewayAgent.isAlive() || !ProcLoginRegisterAgent.isAlive() || !ProcPostsAgent.isAlive()){
//            Thread.sleep(200); // Agents need have time to open process
                System.out.println();
                System.out.println("isAlive APIGatewayAgent: " + ProcAPIGatewayAgent.isAlive());
                System.out.println("isAlive LoginRegisterAgent: " + ProcLoginRegisterAgent.isAlive());
                System.out.println("isAlive PostsAgent: " + ProcPostsAgent.isAlive());
                System.out.println();
//                throw new Exception("Agent has been not opened");
            }
        } catch (IOException e) {
            e.printStackTrace();
            waitForUserInput();
        } catch (Exception e) {
            e.printStackTrace();
            waitForUserInput();
        }








        Runnable myThread2 = () ->
        {
            System.out.println("SERVER THREAD STARTED");
            try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(ManagerPort))) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Agent connected \n");
                    new Thread(new AgentThread(clientSocket)).start();
                }

            } catch (IOException e) {
                System.err.println("500;Login Service ERROR. " + e.getMessage());
                waitForUserInput();
            }
        };

        // Instantiating Thread class by passing Runnable
        // reference to Thread constructor
        Thread run = new Thread(myThread2);

        // Starting the thread
        run.start();

        System.out.println("REQUEST SENDER THREAD STARTED");
        Thread.sleep(1000);

        Boolean isApiGatwayRequestSended = false;
        try {
            if(!isApiGatwayRequestSended){
                System.out.println("Agent0 Im trying to send request to open apiGateway:");
                PrintWriter output2 = new PrintWriter(Agent0.getOutputStream(), true);
                output2.println("type:execution_request;" +
                        "message_id:0;" +
                        "agent_network_address:none;" +
                        "service_name:ApiGateway.jar;" +
                        "service_instance_id:1;" +
                        "socket_configuration:none;" +
                        "plug_configuration:none");
                isApiGatwayRequestSended = true;
                output2.flush();
            }
        }catch(Exception e){

            waitForUserInput();
        }




//            Thread.sleep(1000);

    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }


    static class AgentThread  implements Runnable {
        private final Socket agentSocket;

        public AgentThread(Socket clientSocket) {
            this.agentSocket = clientSocket;
        }

        @Override
        public void run() {

            try (BufferedReader input = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
                 PrintWriter output = new PrintWriter(agentSocket.getOutputStream(), true)
                 ) {
                try {


                      while(true) {
                          String request = input.readLine();
                          if (request != null) {
                              System.out.println("Request: "+ request);
                              String[] userData = request.split(";");
                              String[] requestType = userData[0].split(":");
                              System.out.println("requestType: "+ requestType[1]);
                              if(requestType[1].equals("initiation_request")){
                                  System.out.println("initiation_request has beed detected");
                                  String[] message_id = userData[1].split(":");
                                  if(message_id[1].equals("0")){
                                  System.out.println("AGENT 0 HAS BEEN CONECTED TO MANAGER");
                                      Agent0 = this.agentSocket;
                                  }
                                  if(message_id[1].equals("1")){
                                  System.out.println("AGENT 1 HAS BEEN CONECTED TO MANAGER");
                                      Agent1 = this.agentSocket;
                                  }
                                  if(message_id[1].equals("2")){
                                  System.out.println("AGENT 2 HAS BEEN CONECTED TO MANAGER");
                                      Agent2 = this.agentSocket;
                                  }

                              }
                              if(requestType[1].equals("microserviceadress_request")){
                                  System.out.println("microserviceadress_request detected ");
                                  try {
//                                          System.out.println("Agent0 Im trying to send request to open apiGateway:");
                                          PrintWriter output2 = new PrintWriter(Agent0.getOutputStream(), true);
                                          output2.println("Type:microserviceadress_request;Message_id:9013");
                                          output2.flush();

                                  }catch(Exception e){


                                  }
                              }

                          }
                      }
                }catch (SocketException s){
                    s.printStackTrace();
                }

            }catch(IOException e){
            e.printStackTrace();
            }
         finally
        {
            try {
                agentSocket.close();
            } catch (IOException e) {
                System.err.println("Błąd socketa");
            }
        }
    }


        }

    }











