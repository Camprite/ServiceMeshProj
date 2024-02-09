import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import static java.lang.System.exit;


public class Manager {




    public static Socket[] Agents={null,null,null};
    public static String[][] AgentsServices={null,null,null};

//    static String RequestToCreateApiGatewayMicroservice =
//            "Type:execution_request" +
//            "Message_id:0" +
//            "Service_name:apigateway" +
//            "Agent_type:""" +
//            "Line:9000" +

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("[MANAGER]");
        String ManagerPort = "9100";
        String ManagerIP = "localhost";
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
            waitForUserInput();
        }
        String apiPort = properties.getProperty("api.gateway.port");








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

        try {
            System.out.println("Agent_0 Im trying to send request to open apiGateway:");
            PrintWriter output2 = new PrintWriter(Agents[0].getOutputStream(), true);
            output2.println(new Requests("execution_request","0","apigateway","0",apiPort));
            output2.flush();
        }catch(Exception e){
            waitForUserInput();
        }
        waitForUserInput();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    private static void waitForUserInput() {
        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Scanner scan = new Scanner(System.in);
                String input = "";
                while (true) {

                    input = scan.nextLine();
                    System.out.println("Input: "+input);

                }
            }
        });
        inputThread.start();
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
                              boolean IsItStatus = request.split(";")[2].split(":")[0].compareTo("Status") == 0;
                              if(IsItStatus){
                                  Responses response = new Responses(request);
                                  if(response.Status.equals("200")){
                                      Socket currentSocket=Agents[Integer.parseInt(response.Agent_type)];
                                      PrintWriter currentOutput = new PrintWriter(currentSocket.getOutputStream(), true);
                                      currentOutput.println(response);
                                      currentOutput.flush();
                                      continue;
                                  } else {
                                      Socket currentSocket=null;
                                      for (int i=0;i<3;i++){
                                          for (String service:AgentsServices[i]){
                                              if(service.equals(response.Service_name)){
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
                                      Requests new_request = new Requests(response);
                                      new_request.Type = "execution_request";
                                      new_request.Line = String.valueOf(newPort());
                                      currentOutput.println(new_request);
                                      currentOutput.flush();
                                      new_request.Type = "microserviceadress_request";
                                      new_request.Line = "";
                                      request = new_request.toString();
                                  }
                              }
                              Requests true_request = new Requests(request);
                              if(true_request.Type.equals("initiation_request")){
                                  int index=Integer.parseInt(true_request.Agent_type);
                                  if(index>=0 && index<=2){
                                      System.out.println("AGENT "+index+" HAS BEEN CONECTED TO MANAGER");
                                      Agents[index]=this.agentSocket;
                                      AgentsServices[index]=true_request.Service_name.split(" ");
                                  }
                                output.println(new Responses(true_request,"200"));
                                output.flush();
                              }
                              else if(true_request.Type.equals("microserviceadress_request")){
                                  Socket currentSocket=null;
                                  for (int i=0;i<3;i++){
                                      for (String service:AgentsServices[i]){
                                          if(service.equals(true_request.Service_name)){
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












