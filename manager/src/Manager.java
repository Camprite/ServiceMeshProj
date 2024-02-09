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

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Manager <managerIp> <managerPort>");
            System.exit(1);
        }

        managerIp = args[0];
        managerPort = args[1];

        System.out.println("[MANAGER]");

        // Thread listening for agents
        Runnable agentListenerThread = () -> {
            try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(managerPort))) {
                System.out.println("Waiting for agents to connect...");
                while (true) {
                    Socket agentSocket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));

                    // Read initiation request from agent
                    String initiationRequest = input.readLine();
                    if (initiationRequest != null && initiationRequest.startsWith("initiation_request")) {
                        // Parse agent information
                        String[] requestParts = initiationRequest.split(";");
                        String agentType = requestParts[1];
                        String agentIp = requestParts[2];
                        String agentPort = requestParts[3];

                        // Store agent information
                        agentSockets.put(agentIp, agentPort);

                        System.out.println("Agent " + agentType + " connected. IP: " + agentIp + ", Port: " + agentPort);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error occurred while listening for agents: " + e.getMessage());
            }
        };

        // Starting agent listener thread
        Thread agentListener = new Thread(agentListenerThread);
        agentListener.start();

        // Send initialization request to agent 0
        sendInitializationRequest();

        // Thread processing requests from agents
        Runnable requestProcessingThread = () -> {
            while (true) {
                for (String agentIp : agentSockets.keySet()) {
                    try {
                        String agentPort = agentSockets.get(agentIp);
                        Socket agentSocket = new Socket(agentIp, Integer.parseInt(agentPort));
                        BufferedReader input = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
                        String request = input.readLine();
                        if (request != null) {
                            processRequest(agentIp, request);
                        }
                    } catch (IOException e) {
                        System.err.println("Error occurred while reading request from agent " + agentIp + ": " + e.getMessage());
                    }
                }

                // Check microservices status and close if necessary
                checkMicroservicesStatus();

                try {
                    TimeUnit.MILLISECONDS.sleep(500); // Pause for a while to avoid high CPU usage
                } catch (InterruptedException e) {
                    System.err.println("Thread sleep interrupted: " + e.getMessage());
                }
            }
        };

        // Starting request processing thread
        Thread requestProcessor = new Thread(requestProcessingThread);
        requestProcessor.start();
    }

    private static void sendInitializationRequest() {
        try {
            String agent0Ip = agentSockets.keySet().iterator().next(); // Get IP of the first agent
            String agent0Port = agentSockets.get(agent0Ip);
            Socket agent0Socket = new Socket(agent0Ip, Integer.parseInt(agent0Port));
            PrintWriter output = new PrintWriter(agent0Socket.getOutputStream(), true);
            String initializationRequest = "execution_request;" + agent0Ip + ";9000"; // Use IP of agent 0
            output.println(initializationRequest);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error occurred while sending initialization request to agent 0: " + e.getMessage());
        }
    }

    private static void processRequest(String agentIp, String request) {
        try {
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
                    PrintWriter output = new PrintWriter(agent0Socket.getOutputStream(), true);
                    output.println("microserviceadress_request;" + ip + ";" + port);
                    output.flush();
                    String IPAgent;
                    if (serviceName.equals("login") || serviceName.equals("registration")) {
                        // Wybierz IP i port drugiego agenta
                        IPAgent = agentSockets.keySet().stream().skip(1).findFirst().orElse("localhost");
                    } else {
                        // Wybierz IP i port trzeciego agenta
                        IPAgent = agentSockets.keySet().stream().skip(2).findFirst().orElse("localhost");
                    }
                    String agentPort = agentSockets.get(IPAgent);
                    Socket agentSocket = new Socket(IPAgent, Integer.parseInt(agentPort));
                    PrintWriter out = new PrintWriter(agentSocket.getOutputStream(), true);
                    out.println("execution_request;"+ serviceName+";" + ip + ";" + port);
                    output.flush();
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
                    String ipAddressPort = "localhost:" + ipPort;
                    microservicesDetails.put(ipPort, requestParts[1]);
                    String agent0Ip = agentSockets.keySet().iterator().next();
                    String agent0Port = agentSockets.get(agent0Ip);
                    Socket agent0Socket = new Socket(agent0Ip, Integer.parseInt(agent0Port));
                    PrintWriter output = new PrintWriter(agent0Socket.getOutputStream(), true);
                    output.println(request.replaceFirst("execution_request", "execute_initialization") + ";" + ipPort);
                    output.flush();
                    microservices.put(requestParts[1], "busy");
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while processing request from agent " + agentIp + ": " + e.getMessage());
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
                        if (serviceName.equals("login") || serviceName.equals("registration")) {
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
