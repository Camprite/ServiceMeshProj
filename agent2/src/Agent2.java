import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class Agent2 {

    protected static String agentType;
    protected static String agentIp;
    protected static String agentPort;
    protected static String managerIp;
    protected static String managerPort;
    protected static Process process; // Dodajemy pole przechowujące proces

    public static void main(String[] args) {

        managerIp = args[0];
        managerPort = args[1];
        agentType = args[2];
        System.out.println("[AGENT2 PROCESS]");
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
        agentPort = properties.getProperty("agent2.service.port"); //NOT TO CHANGE
        agentIp = properties.getProperty("agent2.service.ip");
        Thread myThread = new Thread(() -> {
            try {
                try (Socket socket = new Socket(managerIp, Integer.parseInt(managerPort));
                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Register to Manager request
                    output.println("initiation_request" + ';' + agentType + ';' + agentIp + ';' + agentPort);
                    System.out.println("Initiation request has been sent to Manager");
                    System.out.print("Response: ");



                    while (true) {

                        String request = input.readLine();
                        if (request != null) {
                            String[] requestParts = request.split(";");

                            String requestType = requestParts[0];

                            if (requestType.equals("execution_request")) {
                                System.out.println("[REQUEST FROM MANAGER]: " + request);
                                System.out.println("[REQUEST]: execution request has been detected");
                                String Name=requestParts[1];
                                String serviceIp = requestParts[2];
                                String servicePort = requestParts[3];
                                try {
                                    String servicePath = System.getProperty("user.dir") + "\\" + Name + ".jar";
                                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", servicePath,  agentIp, agentPort, servicePort, "localhost", "9000");
                                    process = processBuilder.start(); // Zapisujemy referencję do procesu

                                    // Tworzymy nowy wątek do obsługi komunikacji z mikroserwisem
                                    Thread serviceThread = new Thread(() -> {
                                        try (Socket serviceSocket = new Socket(serviceIp, Integer.parseInt(servicePort));
                                             PrintWriter serviceOutput = new PrintWriter(serviceSocket.getOutputStream(), true);
                                             BufferedReader serviceInput = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()))) {

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
                                    process.destroy(); // Zamykamy proces, jeśli istnieje
                                    process = null; // Czyścimy referencję
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
