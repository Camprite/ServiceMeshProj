import java.io.IOException;
import java.util.Scanner;

public class TestProcesBuilder {
    public static void main(String[] args) throws IOException {
        String PostsAPIPath = System.getProperty("user.dir") + "\\agent.jar";
        String ManagerPath = System.getProperty("user.dir") + "\\manager.jar";
//        String PostsAPIPath = System.getProperty("user.dir");
        System.out.println(PostsAPIPath);
        Scanner scan = new Scanner(System.in);

//
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar",  ManagerPath);
        Process proc = processBuilder.start();
        System.out.println(proc.isAlive());
        System.out.println(proc.pid());
        System.out.println("enter by zamknac");
        scan.nextLine();

        proc.destroy();
        proc.destroyForcibly();

        System.out.println("");
        System.out.println(proc.pid());
        System.out.println(proc.isAlive());

        scan.nextLine();


        //        processBuilder.redirectErrorStream(true);
    }

}
