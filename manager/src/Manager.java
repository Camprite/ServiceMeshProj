import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

//  PRZYDATNE :)
//  netstat -ano | find "9000"
//  taskkill /F /PID your_PID
//




public class Manager {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("SerwisMenagera");
        String APIGatewayPath = System.getProperty("user.dir") + "\\apigateway.jar";
        String LoginAPIPath = System.getProperty("user.dir") + "\\login.jar";
        String RegisterAPIPath = System.getProperty("user.dir") + "\\rejestracja.jar";
        String FilesAPIPath = System.getProperty("user.dir") + "\\pliki.jar";
        String PostsAPIPath = System.getProperty("user.dir") + "\\posty.jar";
        String InterfacePath = System.getProperty("user.dir") + "\\Interfejs.jar";
        System.out.println(APIGatewayPath);

        ProcessBuilder processBuilderLogin = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  LoginAPIPath);


        ProcessBuilder processBuilderRegister = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  RegisterAPIPath);



        ProcessBuilder processBuilderPosts = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  PostsAPIPath);

        ProcessBuilder processBuilderFiles = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  FilesAPIPath);

        ProcessBuilder processBuilder = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  APIGatewayPath);


        ProcessBuilder processBuilderInterfejs = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  InterfacePath);




        try {
            Process processRegister = processBuilderRegister.start();

            Process processGateway = processBuilder.start();

            Process processLogin = processBuilderLogin.start();


            Process  processInterfejs = processBuilderInterfejs.start();

            Process processPosts = processBuilderPosts.start();

            Process processFiles = processBuilderFiles.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
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
