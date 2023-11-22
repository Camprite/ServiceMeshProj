import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

//  PRZYDATNE :)
//  netstat -ano | find "9000"
//  taskkill /F /PID your_PID
//




public class Manager {
    public static void main(String[] args) throws InterruptedException {
        String ManagerPort = "9100";
        String ManagerIP = "localhost";





//        String AgentPath = System.getProperty("user.dir") + "\\apigateway.jar";
        String AgentPath = System.getProperty("user.dir") + "\\agent.jar";

        ProcessBuilder pbAPIGatewayAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9010", "localhost", ManagerPort, ManagerIP);
        ProcessBuilder pbLoginRegisterAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9020", "localhost", ManagerPort, ManagerIP);
        ProcessBuilder pbPostsAgent = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  AgentPath , "9030", "localhost", ManagerPort, ManagerIP);


        try {
            Process ProcAPIGatewayAgent = pbAPIGatewayAgent.start();
            Process ProcLoginRegisterAgent = pbLoginRegisterAgent.start();
            Process ProcPostsAgent = pbPostsAgent.start();
            long pid =  ProcAPIGatewayAgent.pid();
            if(!ProcAPIGatewayAgent.isAlive()||!ProcLoginRegisterAgent.isAlive()||!ProcPostsAgent.isAlive()){
                throw new Exception("Agent has been not opened");
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(ManagerPort))) {
            Thread.sleep(1000);

            while (true) {
    Socket clientSocket = serverSocket.accept();
                System.out.println("NEW CLIENT on port: " + clientSocket.getLocalPort());
                if(clientSocket.getPort() == 9010){
                    new Thread(new AgentConnected(clientSocket,"start;apigateway.jar")).start();
                    System.out.println("port9010");
                }else {
                    new Thread(new AgentConnected(clientSocket,"")).start();

                }

            }
        } catch (IOException e) {
            System.err.println("500;Login Service ERROR. " + e.getMessage());
            waitForUserInput();
        }






        waitForUserInput();


//        Process builder dla 3 agentów
//        Połączenie do 3 agentów
//          Wysłanie agentom żądzanie otwarcia procesów
//
//





//
//        System.out.println("SerwisMenagera");
//        String APIGatewayPath = System.getProperty("user.dir") + "\\apigateway.jar";
//        String LoginAPIPath = System.getProperty("user.dir") + "\\login.jar";
//        String RegisterAPIPath = System.getProperty("user.dir") + "\\rejestracja.jar";
//        String FilesAPIPath = System.getProperty("user.dir") + "\\pliki.jar";
//        String PostsAPIPath = System.getProperty("user.dir") + "\\posty.jar";
//        String InterfacePath = System.getProperty("user.dir") + "\\Interfejs.jar";
//        System.out.println(APIGatewayPath);
//
//        ProcessBuilder processBuilderLogin = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  LoginAPIPath , "9002", "localhost");
//        ProcessBuilder processBuilderLogin2 = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  LoginAPIPath , "9012", "localhost");
//
//
//        ProcessBuilder processBuilderRegister = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  RegisterAPIPath);
//
//
//
//        ProcessBuilder processBuilderPosts = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  PostsAPIPath);
//
//        ProcessBuilder processBuilderFiles = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  FilesAPIPath);
//
//        ProcessBuilder processBuilder = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  APIGatewayPath);
//
//
//        ProcessBuilder processBuilderInterfejs = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  InterfacePath);
//
//
//
//
//        try {
//            Process processRegister = processBuilderRegister.start();
//
//            Process processGateway = processBuilder.start();
//
//            Process processLogin = processBuilderLogin.start();
//            Process processLogin2 = processBuilderLogin2.start();
//
//
//            Process  processInterfejs = processBuilderInterfejs.start();
//
//            Process processPosts = processBuilderPosts.start();
//
//            Process processFiles = processBuilderFiles.start();
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        waitForUserInput();
    }
    private static void waitForUserInput() {
        System.out.println("Press Enter to exit...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
    protected static Process createProcess(String path, int port) throws IOException {
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("java " + path );
            System.out.println(proc.isAlive());
            System.out.println(proc.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return proc;
    }





}
