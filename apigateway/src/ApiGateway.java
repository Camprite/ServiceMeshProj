import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class ApiGateway {
  //  protected static String[] Parts = {"localhost", "8001"};
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ApiGateway <AgentIp> <AgentPort>");
            System.exit(1);
        }

        String agentIp = args[0];
        int agentPort = Integer.parseInt(args[1]);

        System.out.println("ApiGateway");

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
        }

        int gatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port", "9000"));

        try (ServerSocket serverSocket = new ServerSocket(gatewayPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> processRequest(clientSocket, properties, agentIp, agentPort)).start();
            }
        } catch (IOException e) {
            System.err.println("Błąd połączenia z ApiGateway.");
            e.printStackTrace();
        }
    }

    private static void processRequest(Socket clientSocket, Properties properties, String ip, int port) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request;
            while ((request = input.readLine()) != null) {
                String[] parts = request.split(";");
                String serwis = parts[0];
                if(serwis.equals("rejestracja") || serwis.equals("logowanie") || serwis.equals("post") || serwis.equals("czytaj-posts") || serwis.equals("wgraj_plik") || serwis.equals("pobierz_plik")){

                    System.out.println("[REQUEST FROM CLI]: " + request);

                String[] requestParts = request.split(";");
                String requestType = requestParts[0];

                String requestToAgent = "microserviceadress_request" + ";" + requestType;

                    try (Socket socket = new Socket(ip, port);
                         PrintWriter outputAgent = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader inputAgent = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    outputAgent.println(requestToAgent);
                    System.out.println("[WYSLANO REQUEST DO AGENTA]");
                    outputAgent.flush();


                    while (true) {
                        String outputFromAgent = inputAgent.readLine();
                        if (outputFromAgent != null) {
                            System.out.println("[REQUEST FROM AGENT] " + outputFromAgent);
                            String[] Czesci = outputFromAgent.split(";");
                            try (Socket microserviceSocket = new Socket(Czesci[0], Integer.parseInt(Czesci[1]));
                                 PrintWriter outputMicroservice = new PrintWriter(microserviceSocket.getOutputStream(), true);
                                 BufferedReader inputMicroservice = new BufferedReader(new InputStreamReader(microserviceSocket.getInputStream()))) {

                                outputMicroservice.println(request);
                                while(true) {
                                    String responseMicroservice = inputMicroservice.readLine();
                                    if(responseMicroservice!=null) {
                                        output.println(responseMicroservice);
                                        break;
                                    }
                                }
                                break;
                                }catch (IOException e) {
                                System.err.println("Błąd połączenia z mikroserwisem." + e.getMessage());
                            }
                        }

                    }
                } catch (IOException e) {
                    System.err.println("Błąd połączenia z Agentem." + e.getMessage());
                }
            }
        } }catch (IOException e) {
            System.err.println("Błąd wejścia/wyjścia." + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Błąd podczas zamykania gniazda klienta." + e.getMessage());
            }
        }
    }
}
