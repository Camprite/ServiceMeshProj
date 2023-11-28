import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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


 */

public class Manager {




    public static ArrayList<Integer> test = new ArrayList<>();
    public static void main(String[] args) throws InterruptedException {
        String ManagerPort = "9100";
        String ManagerIP = "localhost";





//        String AgentPath = System.getProperty("user.dir") + "\\apigateway.jar";
        String AgentPath = System.getProperty("user.dir") + "\\agent.jar";
        System.out.println(AgentPath);


//          AGENTS TYPES  0 = APIGATEWAY AGENT, 1 = LOGIN + REGISTER AGENT, 2 = POSTS AND FILES AGENT
        ProcessBuilder pbAPIGatewayAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9010", "localhost", ManagerPort, ManagerIP, "0");
        ProcessBuilder pbLoginRegisterAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9020", "localhost", ManagerPort, ManagerIP, "1");
        ProcessBuilder pbPostsAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9030", "localhost", ManagerPort, ManagerIP, "2");

//Opening Agent processes
        try {
            Process ProcAPIGatewayAgent = pbAPIGatewayAgent.start();
            Process ProcLoginRegisterAgent = pbLoginRegisterAgent.start();
            Process ProcPostsAgent = pbPostsAgent.start();
            if(!ProcAPIGatewayAgent.isAlive()||!ProcLoginRegisterAgent.isAlive()||!ProcPostsAgent.isAlive()){
                throw new Exception("Agent has been not opened");
            }
        } catch (IOException e) {
            e.printStackTrace();
            waitForUserInput();
        } catch (Exception e) {
            e.printStackTrace();
            waitForUserInput();
        }


        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(ManagerPort))) {
            Thread.sleep(1000);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Agent connected on port: " + clientSocket.getLocalPort());
                new Thread(new AgentThread(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("500;Login Service ERROR. " + e.getMessage());
            waitForUserInput();
        }



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
                 PrintWriter output = new PrintWriter(agentSocket.getOutputStream(), true)) {

                String request = input.readLine();
                String[] userData = request.split(":");
                System.out.println(userData[0]);
                System.out.println(userData[1]);

            } catch (IOException e) {
                System.err.println("Błąd." + e.getMessage());
            } finally {
                try {
                    agentSocket.close();
                } catch (IOException e) {
                    System.err.println("Błąd socketa");
                }
            }

        }
    }




}





