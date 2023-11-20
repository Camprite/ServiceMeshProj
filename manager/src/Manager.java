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
        String APIGatewayPath = System.getProperty("user.dir") + "\\apigateway.jar";
        String LoginAPIPath = System.getProperty("user.dir") + "\\login.jar";
        String RegisterAPIPath = System.getProperty("user.dir") + "\\rejestracja.jar";
        String FilesAPIPath = System.getProperty("user.dir") + "\\pliki.jar";
        String PostsAPIPath = System.getProperty("user.dir") + "\\posty.jar";
        String InterfacePath = System.getProperty("user.dir") + "\\Interfejs.jar";
        System.out.println(APIGatewayPath);

        ProcessBuilder processBuilder = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  APIGatewayPath);
        processBuilder.redirectErrorStream(true);

        ProcessBuilder processBuilderLogin = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  LoginAPIPath);
        processBuilderLogin.redirectErrorStream(true);

        ProcessBuilder processBuilderRegister = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  RegisterAPIPath);
        processBuilderRegister.redirectErrorStream(true);

        ProcessBuilder processBuilderInterfejs = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  InterfacePath);
        processBuilderRegister.redirectErrorStream(true);

        ProcessBuilder processBuilderPosts = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  PostsAPIPath);
        processBuilderRegister.redirectErrorStream(true);

        ProcessBuilder processBuilderFiles = new ProcessBuilder( "cmd", "/c", "start", "java", "-jar",  FilesAPIPath);
        processBuilderRegister.redirectErrorStream(true);


        try {
            Process process = processBuilder.start();

            Process processLogin = processBuilderLogin.start();

            Process processRegister = processBuilderRegister.start();

            Process  processInterfejs = processBuilderInterfejs.start();

            Process processPosts = processBuilderPosts.start();

            Process processFiles = processBuilderFiles.start();
            System.out.println(process.toString());



            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader readerLogin = new BufferedReader(new InputStreamReader(processLogin.getInputStream()));
            BufferedReader readerRegister = new BufferedReader(new InputStreamReader(processRegister.getInputStream()));
            String line = null;
            String lineLogin = null;
            String lineRegister = null;
            while ((line = reader.readLine()) != null || (lineLogin = readerLogin.readLine()) != null || (lineRegister = readerRegister.readLine()) != null ) {
                System.out.println("[API] isAlive: " + process.isAlive());
                System.out.println("[LOGIN] isAlive: " + processLogin.isAlive());
                System.out.println("[REGISTER] isAlive: " + processRegister.isAlive());
                System.out.println("[API]" + line);
                System.out.println("[LOGIN]" + lineLogin);
                System.out.println("[REGISTER]" + lineRegister);
            }
            // Oczekiwanie na zakończenie procesu
            int exitCode = process.waitFor();
            int exitCodeLogin = processLogin.waitFor();
            int exitCodeRegister = processRegister.waitFor();
            System.out.println("[API]Proces zakończony z kodem: " + exitCode);
            System.out.println("[LOGIN]Proces zakończony z kodem: " + exitCodeLogin);
            System.out.println("[REGISTER]Proces zakończony z kodem: " + exitCodeRegister);

        } catch (IOException | InterruptedException e) {
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
