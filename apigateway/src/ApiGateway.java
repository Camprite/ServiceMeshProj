import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class ApiGateway {




    public static void main(String[] args) {

        String AgentIp = args[0];
        String AgentPort = args[1];


        System.out.println("ApiGateway");

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
            waitForUserInput();
        }
        try (Socket socket = new Socket("localhost", 9010);
        PrintWriter outputAgent = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            outputAgent.write("Message from api;:");
            outputAgent.flush();
            System.out.println("połączono");
            socket.close();
            Thread.sleep(3000);

        } catch (IOException | InterruptedException e) {
            System.err.println("Błąd połączenia z Agentem." + e.getMessage());
            waitForUserInput();
        }


        int gatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port")); //NOT TO CHANGE

        try (ServerSocket serverSocket = new ServerSocket(gatewayPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> processRequest(clientSocket, properties,AgentIp,AgentPort)).start();
            }
        } catch (IOException e) {
            System.out.println(gatewayPort);
            e.printStackTrace();
            System.err.println("Błąd połączenia z ApiGateway.");
            waitForUserInput();
        }

    }

    private static void processRequest(Socket clientSocket, Properties properties,String ip, String port) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = input.readLine();
            System.out.println("[REQUEST FROM CLI]: " + request);

            String[] requestParts = request.split(";");
            String requestType = requestParts[0].split(":")[1];
            System.out.println(requestType);

            String targetServicePort;
            String targetServiceIP;


            String  requestToAgent = "Type:microserviceadress_request;" +
            "Message_id:" + requestType;
            System.out.println("[Request For Microservice Adress]: " + requestToAgent);

            try (Socket socket = new Socket("localhost", 9010);
                 PrintWriter outputAgent = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader inputAgent = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                outputAgent.println(requestToAgent);
                System.out.println("[WYSLANO REQUEST DO AGENTA]" );
                outputAgent.flush();


                String agentResponse = inputAgent.readLine();
                int microservicePort = Integer.parseInt(agentResponse.split(":")[1]);
                System.out.println("[Received Microservice Port]: " + microservicePort);
                //To do: send request to microservice

            } catch (IOException e) {
                System.err.println("Błąd połączenia z Agentem." + e.getMessage());
                waitForUserInput();
            }

            switch (requestType) {
                case "rejestracja" -> {
                    targetServicePort = properties.getProperty("registration.service.port");
                    targetServiceIP = properties.getProperty("registration.service.ip");
                }
                case "logowanie" -> {
                    System.out.println("[LOGIN REQUEST]");
                    targetServicePort = properties.getProperty("login.service.port");
                    targetServiceIP = properties.getProperty("login.service.ip");
                }
                case "post", "czytaj-posts" -> {
                    targetServicePort = properties.getProperty("post.service.port");
                    targetServiceIP = properties.getProperty("post.service.ip");
                }
                case "wgraj_plik", "pobierz_plik" -> {
                    targetServicePort = properties.getProperty("file.service.port");
                    targetServiceIP = properties.getProperty("file.service.ip");
                }
                default -> {
                    System.out.println("Błąd. Nieznany typ zapytania.");
                    return;
                }
            }

            int targetPort = Integer.parseInt(targetServicePort);

            if (requestType.equals("wgraj_plik")) {
                try (Socket targetSocket = new Socket(targetServiceIP, targetPort);
                     PrintWriter targetOutput = new PrintWriter(targetSocket.getOutputStream(), true);
                     BufferedReader targetInput = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()))) {
                    String[] parts = request.split(" ");
                    String destinationFileName = parts[parts.length - 2];
                    String encodedFile = parts[parts.length - 1];

                    targetOutput.println("wgraj_plik;" + destinationFileName + ";" + encodedFile);

                    String response = targetInput.readLine();
                    output.print(response);
                    output.flush();
                } catch (IOException e) {
                    System.err.println("Błędne przekazanie plików ");
                }
            } else if (requestType.equals("pobierz_plik")) {
                try (Socket targetSocket = new Socket(targetServiceIP, targetPort)) {
                    PrintWriter targetOutput = new PrintWriter(targetSocket.getOutputStream(), true);
                    BufferedReader targetInput = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()));

                    targetOutput.println(request);

                    String response = targetInput.readLine();
                    output.println(response);
                    output.flush();
                } catch (IOException e) {
                    System.err.println("Błędne żądanie przekierowania.");
                    waitForUserInput();
                }
            } else {
                try (Socket targetSocket = new Socket(targetServiceIP, targetPort)) {
                    PrintWriter targetOutput = new PrintWriter(targetSocket.getOutputStream(), true);
                    BufferedReader targetInput = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()));

                    targetOutput.println(request);
                    System.out.println("Otrzymano połączenie");
                    String response = targetInput.readLine();
                    System.out.println("TO ODPOWIEDZ "+ response);
                    output.print(response);
                    output.flush();

                } catch (IOException e) {
                    System.err.println("Błędne żądanie przekierowania.");
                    e.printStackTrace();
                    waitForUserInput();
                }

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