import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import static java.lang.System.exit;

//Braki w ssmmp
// W zapytaniach do agenta powinno być pole na ich id by dało się ich łatwo identyfikować
// uzywam aktualnie Agent_type
//
public class Manager {




//    public static Socket[] Agents={null,null,null};
    public static Agents agents = new Agents();

    //    public static String[][] AgentsServices={null,null,null};
    public static Process[] AgentProcesses={null,null,null};

//    public static boolean firstAgent = false;

//    static String RequestToCreateApiGatewayMicroservice =
//            "Type:execution_request" +
//            "Message_id:0" +
//            "Service_name:apigateway" +
//            "Agent_type:""" +
//            "Line:9000" +

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("[MANAGER]");
//        String[] args =
//        [0] = Manager IP
//        [1] = Manager Port



        String ManagerPort = args[0];
        String ManagerIP = args[1];
//        String AgentPath = System.getProperty("user.dir") + "\\agent.jar";


//          AGENTS TYPES  0 = APIGATEWAY AGENT, 1 = LOGIN + REGISTER AGENT, 2 = POSTS AND FILES AGENT
//        ProcessBuilder pbAPIGatewayAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9010", "localhost", ManagerPort, ManagerIP, "0");
//        ProcessBuilder pbLoginRegisterAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9020", "localhost", ManagerPort, ManagerIP, "1");
//        ProcessBuilder pbPostsAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9030", "localhost", ManagerPort, ManagerIP, "2");
//            Process ProcAPIGatewayAgent = pbAPIGatewayAgent.start();
//            AgentProcesses[0] = ProcAPIGatewayAgent;
//            Process ProcLoginRegisterAgent = pbLoginRegisterAgent.start();
//            AgentProcesses[1] = ProcLoginRegisterAgent;
//            Process ProcPostsAgent = pbPostsAgent.start();
//            AgentProcesses[2] = ProcPostsAgent;

        Runnable AgentListenerTherad = () ->
        {
            System.out.println("Thread listening for Agents ");
            try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(ManagerPort))) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Agent connected \n");
                    new Thread(new AgentThread(clientSocket)).start();
                }

            } catch (IOException e) {
                System.err.println("500; Thread listening for Agenst occurs error, none more agents will be connected. " + e.getMessage());

            }
        };

        // Instantiating Thread class by passing Runnable
        // reference to Thread constructor
        Thread run = new Thread(AgentListenerTherad);

        // Starting the listner
        run.start();




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
                              //        First connected agent will have request for open api gateway microservice
//                              if(AgentsAmount == 0) {
//                                  //            Open api gateway request
//                                  System.out.println(" Im trying to send request to open apiGateway by first Agent:");
//                                  output.println(new Requests("execution_request", "0", "apigateway",""));
//                                  output.flush();
//                              }

                              //        Request reading
                              String request = input.readLine();
                              if (request != null) {
                                  System.out.println("Request: "+ request);
//                                  Checking if its only response
                                  boolean IsItStatus = request.split(";")[2].split(":")[0].compareTo("Status") == 0;

                                  if(IsItStatus){
                                      Responses response = new Responses(request);
                                      if(response.Status.equals("200")){
//                                          Socket currentSocket=Agents.get(Integer.parseInt(response.Agent_type));
                                          Socket currentSocket=Agents.findSocketByAgentType(Integer.parseInt(response.Agent_type));
                                          PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                          currentOutput.println(response);
                                          currentOutput.flush();
                                          continue;
                                      } else {
                                          Socket currentSocket=null;
                                          currentSocket = agents.getSocketWithSpecificMicroservice(response.Service_name);

                                          try{
                                              if(currentSocket==null){
                                                  throw new Exception("Wrong service name!!!");
                                              }}
                                          catch (Exception ignore){
                                          }
                                          PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                          Requests new_request = new Requests(response);
                                          new_request.Type = "execution_request";
    //                                      new_request.Line = String.valueOf(newPort()); // porty mają być definiowane po stronie microservicu
                                          currentOutput.println(new_request);
                                          currentOutput.flush();
                                          new_request.Type = "microserviceadress_request";
                                          new_request.Line = "";
                                          request = new_request.toString();
                                      }
                                  }
//                              Checking if its request

                                  Requests true_request = new Requests(request);
//                                 INITIATION REQUEST
                                  if(true_request.Type.equals("initiation_request")){
                                      System.out.println("AGENT HAS BEEN CONECTED TO MANAGER");
//                                      agent need to send - ip, port, type of agent, list of using microservices,
                                      String ip = true_request.getIpFromInitialRequest();
                                      String port = true_request.getPortFromInitialRequest();
                                      String typeOfAgent = true_request.Message_id;
                                      ArrayList<String> listOfMicroservices = true_request.getListOfMicroservices();
                                      Agent newAgent = new Agent(ip,port,typeOfAgent,listOfMicroservices, this.agentSocket);

                                      agents.addAgent(newAgent);

                                    output.println(new Responses(true_request,"200"));
                                    output.flush();
                                  }
//                                  MICROSERVICE ADRESS REQUEST
                                  else if(true_request.Type.equals("microserviceadress_request")){
//                                      REQUEST FORWARDING
                                      Socket currentSocket=null;
                                      try {
//                                          Finding for agent which is responsible for microservice
                                          int AgentNumber = 0;


                                            if(currentSocket==null){
                                                throw new Exception("Wrong service name");
                                            }

                                            PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                            currentOutput.println(request);
                                            currentOutput.flush();
//                                            Cannot find specific microservice error
                                      }catch(Exception e){
                                          System.out.println("Cannot find specific microservice error");
                                          System.out.println(e.getMessage());
                                      }
                                  }
                              }
                          }
//                           Getting error while while(true) loop
                     }catch (SocketException s){

                        s.printStackTrace();
                    }
//                          Getting reader/writer try catch
            }catch(IOException e){
                System.out.println("Getting error while opening reader/writer");
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












