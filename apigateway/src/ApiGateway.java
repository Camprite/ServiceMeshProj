import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class ApiGateway {


    protected static String AgentIp,AgentPort,APIIp,APIPort;

    public static void main(String[] args) {

        AgentIp = args[0];
        AgentPort = args[1];

        System.out.println("ApiGateway");

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
            waitForUserInput();
        }
        try (Socket socket = new Socket(AgentIp, Integer.parseInt(AgentPort))) {
            System.out.println("połączono");
            socket.close();
            Thread.sleep(3000);

        } catch (IOException | InterruptedException e) {
            System.err.println("Błąd połączenia z Agentem." + e.getMessage());
            waitForUserInput();
        }


        int gatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port")); //NOT TO CHANGE
//        int gatewayPort = Integer.parseInt(ApiPort);
        try (ServerSocket serverSocket = new ServerSocket(gatewayPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> processRequest(clientSocket,AgentIp,AgentPort)).start();
            }
        } catch (IOException e) {
            System.out.println(gatewayPort);
            e.printStackTrace();
            System.err.println("Błąd połączenia z ApiGateway.");
            waitForUserInput();
        }

    }

    private static void processRequest(Socket clientSocket,String ip, String port) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = input.readLine();
            System.out.println("[REQUEST FROM CLI]: " + request); //[REQUEST FROM CLI]: Type:logowanie;Message_id:1444838425;Line:daw;daw
            Requests new_request = new Requests(request);

            String targetServicePort="";
            String targetServiceIP="";

            Requests requestToAgent = new Requests("microserviceadress_request",new_request.Message_id,new_request.Service_name,new_request.Agent_type,"");
            System.out.println("[Request For Microservice Adress]: " + requestToAgent);

            try (Socket socket = new Socket(AgentIp, Integer.parseInt(AgentPort));
                 PrintWriter outputAgent = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader inputAgent = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                outputAgent.println(requestToAgent);
                System.out.println("[WYSLANO REQUEST DO AGENTA]");
                outputAgent.flush();
                String response=inputAgent.readLine();
                System.out.println("Response: "+response);
                Responses new_response = new Responses(response);
                targetServiceIP=new_response.Line.split(";")[0];
                targetServicePort=new_response.Line.split(";")[1];
            } catch (IOException e) {
                System.err.println("Błąd połączenia z Agentem." + e.getMessage());
                waitForUserInput();
            }


            int targetPort = Integer.parseInt(targetServicePort);
            try {
                Thread.sleep(2000);
            } catch (Exception ignore){

            }

                try (Socket targetSocket = new Socket(targetServiceIP, targetPort);
                     PrintWriter targetOutput = new PrintWriter(targetSocket.getOutputStream(), true);
                     BufferedReader targetInput = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()))) {

                    targetOutput.println(new_request);

                    String response = targetInput.readLine();
                    output.print(response);
                    output.flush();
                } catch (IOException e) {
                    System.err.println("Błędne przekazanie plików ");
                }

        } catch (IOException e) {
            System.err.println("Błąd przetwarzania żądania");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Wewnętrzny błąd serwera" + e.getMessage());
                waitForUserInput();
            }
        }
    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
}