import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class Agent0 {

    protected static String agentType;
    protected static String agentIp;
    protected static String agentPort;
    protected static String managerIp;
    protected static String managerPort;
    protected static String ApiIp;
    protected static String ApiPort;

    public static void main(String[] args) {
        managerIp = args[0];
        managerPort = args[1];
        agentType = args[2];
        System.out.println("[AGENT-0 PROCESS]");
        System.out.println("-----");
        System.out.println(managerIp);
        System.out.println(managerPort);
        System.out.println(agentType);

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
        }
        agentPort = properties.getProperty("agent0.service.port"); //NOT TO CHANGE
        agentIp = properties.getProperty("agent0.service.ip");

        ApiPort = properties.getProperty("api.gateway.port"); //NOT TO CHANGE
        ApiIp = properties.getProperty("api.gateway.ip");

        Thread myThread = new Thread(() -> {
            try {
                try (Socket socket = new Socket(managerIp, Integer.parseInt(managerPort));
                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Register to Manager request
                    output.println("initiation_request" + ';' + agentType + ';' + agentIp + ';' + agentPort);
                    System.out.println("Initiation request has been sent to Manager");

                    while (true) {
                        String request = input.readLine();
                        System.out.println(request);
                        if (request != null) {
                            String[] requestParts = request.split(";");
                            String requestType = requestParts[0];

                            if (requestType.equals("execution_request")) {
                                System.out.println("[REQUEST FROM MANAGER]: " + request);
                                System.out.println("[REQUEST]: execution request has been detected");

                                try {
                                    String servicePath = System.getProperty("user.dir") + "\\" + "ApiGateway" + ".jar";
                                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", servicePath, agentIp, agentPort);
                                    Process process = processBuilder.start();
                                    break; // Zakończ pętlę po uruchomieniu procesu
                                } catch (Exception e) {
                                    System.out.println("[AGENT FAILED] Processbuilder failed");
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    }

                    try (Socket Apisocket = new Socket(ApiIp, Integer.parseInt(ApiPort));
                         PrintWriter out = new PrintWriter(Apisocket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(Apisocket.getInputStream()))) {

                        while (true) {
                            String Apirequest = in.readLine();
                            if (Apirequest != null) {
                                System.out.println("[REQUEST FROM API]: " + Apirequest);
                                String[] requestPartsApi = Apirequest.split(";");
                                String requestTypeApi = requestPartsApi[0];

                                if (requestTypeApi.equals("microserviceadress_request")) {
                                    output.println(Apirequest);
                                }
                            }

                            String request = input.readLine();
                            if (request != null) {
                                String[] requestParts = request.split(";");
                                String requestType = requestParts[0];

                                if (requestType.equals("microserviceadress_request")) {
                                    out.println(request);
                                }
                            }
                        }
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        });
        myThread.start();
    }
}
