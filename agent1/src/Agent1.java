import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class Agent1 {

    protected static String agentType;
    protected static String agentIp;
    protected static String agentPort;
    protected static String managerIp;
    protected static String managerPort;
    protected static Process process;

    public static void main(String[] args) {

        managerIp = args[0];
        managerPort = args[1];
        agentType = args[2];

        System.out.println("[AGENT1 PROCESS]");
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
        agentPort = properties.getProperty("agent1.service.port");
        agentIp = properties.getProperty("agent1.service.ip");

        Thread myThread = new Thread(() -> {
            try {
                try (Socket socket = new Socket(managerIp, Integer.parseInt(managerPort));
                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    output.println("initiation_request" + ';' + agentType + ';' + agentIp + ';' + agentPort);
                    System.out.println("Initiation request has been sent to Manager");
                    System.out.print("Response: ");
                    System.out.println(input.readLine());
                    ServerSocket serviceSocket = new ServerSocket(Integer.parseInt(agentPort));
                    Socket clientSocket = serviceSocket.accept();
                    PrintWriter serviceOutput = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader serviceInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    while (true) {
                        String request;

                        if ((request = serviceInput.readLine()) != null) {
                            String[] requestParts = request.split(";");

                            String requestType = requestParts[0];

                            if (requestType.equals("execution_request")) {
                                System.out.println("[REQUEST FROM MANAGER]: " + request);
                                System.out.println("[REQUEST]: execution request has been detected");
                                String name = requestParts[1];
                                String serviceIp = requestParts[2];
                                String servicePort = requestParts[3];

                                try {
                                    String servicePath = System.getProperty("user.dir") + "\\" +name +".jar";
                                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", servicePath, agentIp, agentPort, servicePort, "localhost", "9000");
                                    process = processBuilder.start();
                                    Thread serviceThread = new Thread(() -> {
                                        try (Socket service = new Socket(serviceIp, Integer.parseInt(servicePort));
                                             PrintWriter out = new PrintWriter(service.getOutputStream(), true);
                                             BufferedReader inp = new BufferedReader(new InputStreamReader(service.getInputStream()))) {

                                            while (true) {
                                                String serviceRequest = serviceInput.readLine();
                                                if (serviceRequest != null) {
                                                    output.println(serviceRequest);
                                                    break;
                                                }
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });

                                    serviceThread.start(); // Uruchamiamy wątek obsługi mikroserwisu


                                } catch (Exception e) {
                                    System.out.println("[AGENT FAILED] Processbuilder failed");
                                    System.out.println(e.getMessage());
                                }
                            } else if (requestType.equals("finish_request")) {
                                System.out.println("[REQUEST FROM MANAGER]: " + request);
                                System.out.println("[REQUEST]: close_microservice request has been detected");
                                if (process != null) {
                                    process.destroy();
                                    process = null;
                                }
                            }
                        }
                    }

                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } finally {

            }

        });
        myThread.start();
    }
}
