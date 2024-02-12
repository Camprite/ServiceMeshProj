import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Manager {

    protected static String managerIp;
    protected static String managerPort;
    protected static Map<String, String> agentSockets = new HashMap<>();
    protected static Map<String, String> microservices = new HashMap<>();
    protected static Map<String, String> microservicesDetails = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: java Manager <managerIp> <managerPort>");
            System.exit(1);
        }

        managerIp = args[0];
        managerPort = args[1];

        System.out.println("[MANAGER]");

        // Thread listening for agents

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(managerPort))) {
            System.out.println("Waiting for agents to connect...");
            boolean agentType0Received = false;
            boolean agentType1Received = false;
            boolean agentType2Received = false;

            while (true) {
                Socket agentSocket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
                PrintWriter output = new PrintWriter(agentSocket.getOutputStream(), true);

                // Read initiation request from agent
                String initiationRequest = input.readLine();

                if (initiationRequest != null && initiationRequest.startsWith("initiation_request")) {
                    // Parse agent information
                    String[] requestParts = initiationRequest.split(";");
                    String agentType = requestParts[1];
                    String agentIp = requestParts[2];
                    String agentPort = requestParts[3];

                    if (agentType.equals("1") && !agentType1Received) {
                        agentType1Received = true;
                    } else if (agentType.equals("2") && !agentType2Received) {
                        agentType2Received = true;
                    } else if (agentType.equals("0") && !agentType0Received) {
                        String initializationRequest = "execution_request;" + "localhost" + ";9000"; // Use IP of agent 0
                        output.println(initializationRequest);
                        agentType0Received = true;
                    }

                    agentSockets.put(agentPort, agentIp);

                    System.out.println("Agent " + agentType + " connected. IP: " + agentIp + ", Port: " + agentPort);
                    // Check if all agent types have been received, then break the loop
                    if (agentType0Received && agentType1Received && agentType2Received) {
                        break;
                    }

                    output.println("Initialization request received. Waiting for all agents to connect...");
                }
            }

        } catch (IOException e) {
            System.err.println("Error occurred while listening for agents: " + e.getMessage());
        }

        // Starting request processing thread
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(managerPort))) {
            System.out.println("Waiting for requests from agents...");

            while (true) {
                Socket agentSocket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
                PrintWriter output = new PrintWriter(agentSocket.getOutputStream(), true);

                String request = input.readLine();
                System.out.println("Received request from agent: " + request);

                if (request != null) {
                    String[] parts = request.split(";");
                    String agentPort = parts[2];
                    String serwis = parts[1];
                    if(serwis.equals("rejestracja") || serwis.equals("logowanie") || serwis.equals("post") || serwis.equals("czytaj-posts") || serwis.equals("wgraj_plik") || serwis.equals("pobierz_plik")){

                        processRequest(agentPort, request, serverSocket, output);
                        //Zabezpieczenie poki nie ma id wiadomosci
                    }
                }
              //  checkMicroservicesStatus();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while listening for requests from agents: " + e.getMessage());
        }



        // Check microservices status and close if necessary

    }

    private static void processRequest(String agentPort, String request, ServerSocket serverSocket, PrintWriter output) {
        try {
            System.out.println(request);
            String[] requestParts = request.split(";");
            String requestType = requestParts[0];

            if (requestType.equals("microserviceadress_request")) {
                String serviceName = requestParts[1];
                if (microservices.containsKey(serviceName) && microservices.get(serviceName).equals("free")) {
                    String ipAddressPort;
                    String ipPort;
                    if (microservicesDetails.containsValue(serviceName)) {
                        String finalServiceName = serviceName;
                        ipPort = microservicesDetails.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(finalServiceName))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(null);
                        ipAddressPort = microservicesDetails.get(ipPort);
                    } else {
                        int nextPort = microservicesDetails.keySet().stream()
                                .mapToInt(Integer::parseInt)
                                .max()
                                .orElse(8000) + 1;
                        ipPort = String.valueOf(nextPort);
                        ipAddressPort = "localhost:" + ipPort;
                        microservicesDetails.put(ipPort, serviceName);
                    }

                    String[] ipPortArray = ipPort.split(":");
                    String ip = ipPortArray[0];
                    int port = Integer.parseInt(ipPortArray[1]);

                    String agent0Ip = agentSockets.keySet().iterator().next();
                    String agent0Port = agentSockets.get(agent0Ip);
                    Socket agent0Socket = new Socket(agent0Ip, Integer.parseInt(agent0Port));
                     output = new PrintWriter(agent0Socket.getOutputStream(), true);
                   output.println("microserviceadress_request;" + ip + ";" + port);
                    output.flush();

                    String agentIp = agentSockets.get(agentPort);
                    Socket agentSocket = new Socket(agentIp, Integer.parseInt(agentPort));
                    PrintWriter out = new PrintWriter(agentSocket.getOutputStream(), true);
                    out.println("execution_request;" + serviceName + ";" + ip + ";" + port);
                    out.flush();

                    microservices.put(serviceName, "busy");
                } else if (requestType.equals("finish_request")) {
                    serviceName = requestParts[1];
                    String portToFree = requestParts[3];
                    for (Map.Entry<String, String> entry : microservicesDetails.entrySet()) {
                        if (entry.getValue().equals(serviceName)) {
                            microservicesDetails.remove(entry.getKey());
                            microservices.put(serviceName, "free");
                            break;
                        }
                    }
                } else {
                    int nextPort = microservicesDetails.keySet().stream()
                            .mapToInt(Integer::parseInt)
                            .max()
                            .orElse(8000) + 1;
                    String ipPort = String.valueOf(nextPort);
                    microservicesDetails.put(ipPort, requestParts[1]);

                    String agent0Ip = agentSockets.keySet().iterator().next();
                    String agent0Port = agentSockets.get(agent0Ip);

                    output.println("localhost" +";"+ipPort);
                    output.flush();

                    String agentIp;
                    String agentPorts;
                    if (serviceName.equals("SerwisLogowania") || serviceName.equals("SerwisRejestracji")) {
                        // Jeśli serviceName to login lub rejestracja, wybierz IP i port drugiego agenta
                        agentIp = agentSockets.keySet().stream().skip(1).findFirst().orElse("localhost");
                         agentPorts = agentSockets.get(agentIp);
                    } else {
                        // W przeciwnym razie wybierz IP i port trzeciego agenta
                       agentIp = agentSockets.keySet().stream().skip(2).findFirst().orElse("localhost");
                         agentPorts = agentSockets.get(agentIp);
                    }
                    microservices.put(requestParts[1], "busy");
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while processing request from agent " + agentPort + ": " + e.getMessage());
        }
    }

    private static void checkMicroservicesStatus() {
        for (String serviceName : microservices.keySet()) {
            if (microservices.get(serviceName).equals("busy")) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    if (microservices.get(serviceName).equals("busy")) {
                        microservices.remove(serviceName);
                        System.out.println("Closed microservice: " + serviceName);
                        String agentIp;
                        if (serviceName.equals("SerwisLogowania") || serviceName.equals("SerwisRejestracji")) {
                            agentIp = agentSockets.keySet().stream().skip(1).findFirst().orElse("localhost");
                        } else {
                            agentIp = agentSockets.keySet().stream().skip(2).findFirst().orElse("localhost");
                        }
                        String agentPort = agentSockets.get(agentIp);
                        Socket agentSocket = new Socket(agentIp, Integer.parseInt(agentPort));
                        PrintWriter output = new PrintWriter(agentSocket.getOutputStream(), true);
                        output.println("close_microservice");
                        output.flush();
                    }
                } catch (InterruptedException | IOException e) {
                    System.err.println("Error occurred while waiting to check microservice status or sending close_microservice message: " + e.getMessage());
                }
            }
        }
    }
}
