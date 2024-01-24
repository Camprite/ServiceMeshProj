import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.Random;
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




    public static Socket[] Agents={null,null,null};
    public static String[][] AgentsServices={null,null,null};

    public static int maxCreate=0;

//    static String RequestToCreateApiGatewayMicroservice =
//            "type:execution_request \n" +
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

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
            waitForUserInput();
        }
        int apiPort = Integer.parseInt(properties.getProperty("api.gateway.port"));


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
                PrintWriter output2 = new PrintWriter(Agents[0].getOutputStream(), true);
                output2.println("type:execution_request;" +
                        "message_id:0;" +
                        "agent_network_address:none;" +
                        "service_name:apigateway;" +
                        "service_instance_id:1;" +
                        "socket_configuration:"+apiPort+";" +
                        "plug_configuration:none");
                isApiGatwayRequestSended = true;
                output2.flush();
            }
        }catch(Exception e){

            waitForUserInput();
        }
        while (true){
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
                              String requestType = userData[0].split(":")[1];
                              boolean enter=true;
                              if(userData[userData.length-1].split(":")[0].compareTo("Status")==0){
                                  if(userData[userData.length-1].split(":")[1].compareTo("200")==0){
                                      Socket currentSocket=Agents[Integer.parseInt(userData[2].split(":")[1])];
                                      PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                      currentOutput.println(request);
                                      currentOutput.flush();
                                      enter=false;
                                  } else {
                                      String serviceName = userData[3].split(":")[1];
                                      if(serviceName.compareTo("logowanie")==0){
                                          serviceName="login";
                                      } else if(serviceName.compareTo("post")==0 || serviceName.compareTo("czytaj-posts")==0){
                                          serviceName="posty";
                                      } else if(serviceName.compareTo("wgraj_plik")==0 || serviceName.compareTo("pobierz_plik")==0){
                                          serviceName="pliki";
                                      }
                                      Socket currentSocket=null;
                                      for (int i=0;i<3;i++){
                                          for (String service:AgentsServices[i]){
                                              if(service.compareTo(serviceName)==0){
                                                  currentSocket=Agents[i];
                                                  break;
                                              }
                                          }
                                      }
                                      try{
                                      if(currentSocket==null){
                                          throw new Exception("Wrong service name!!!");
                                      }}
                                      catch (Exception ignore){

                                      }
                                      PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                      maxCreate++;
                                      if(maxCreate>=5){
                                          continue;
                                      }
                                      int newPort=newPort();
                                      currentOutput.println("type:execution_request;" +
                                              "message_id:0;" +
                                              "agent_network_address:none;" +
                                              "service_name:"+serviceName+";" +
                                              "service_instance_id:1;" +
                                              "socket_configuration:"+newPort+";" +
                                              "plug_configuration:none");
                                      currentOutput.flush();
                                      StringBuilder sb=new StringBuilder(userData[0]);
                                      for (int i=1;i<4;i++){
                                          sb.append(";"+userData[i]);
                                      }
                                      request=sb.toString();
                                      userData = request.split(";");
                                  }
                              }
                              if(requestType.equals("initiation_request")){
                                  int index=Integer.parseInt(userData[2].split(":")[1]);
                                  if(index>=0 && index<=2){
                                      System.out.println("AGENT "+index+" HAS BEEN CONECTED TO MANAGER");
                                      Agents[index]=this.agentSocket;
                                      AgentsServices[index]=userData[4].split(":")[1].split(" ");
                                  }
                                output.println(request+";Status:200");
                                output.flush();
                              }
                              else if(requestType.equals("microserviceadress_request") && enter){
                                  String serviceName = userData[3].split(":")[1];
                                  if(serviceName.compareTo("logowanie")==0){
                                      serviceName="login";
                                  } else if(serviceName.compareTo("post")==0 || serviceName.compareTo("czytaj-posts")==0){
                                      serviceName="posty";
                                  } else if(serviceName.compareTo("wgraj_plik")==0 || serviceName.compareTo("pobierz_plik")==0){
                                      serviceName="pliki";
                                  }
                                  Socket currentSocket=null;
                                  for (int i=0;i<3;i++){
                                      for (String service:AgentsServices[i]){
                                          if(service.compareTo(serviceName)==0){
                                              currentSocket=Agents[i];
                                              break;
                                          }
                                      }
                                  }
                                  try {
                                        if(currentSocket==null){
                                            throw new Exception("Wrong service name!!!");
                                        }
                                        PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                        currentOutput.println(request);
                                        currentOutput.flush();
                                  }catch(Exception e){
                                      System.out.println(e.getMessage());
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

        public static int newPort(){
            Random rand=new Random();
            int result;
            while (true){
                result=rand.nextInt(8000,8999);
                ServerSocket ss = null;
                DatagramSocket ds = null;
                try {
                    ss = new ServerSocket(result);
                    ss.setReuseAddress(true);
                    ds = new DatagramSocket(result);
                    ds.setReuseAddress(true);
                    return result;
                } catch (IOException ignored) {
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                    if (ss != null) {
                        try {
                            ss.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
    }












