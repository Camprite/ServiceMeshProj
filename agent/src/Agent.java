import java.util.Scanner;

public class Agent {
    public static void main(String[] args) {
        System.out.println(args);
        for (String s :args
             ) {
            System.out.println(s);
        }
        System.out.println("TEST");
        Scanner scanner = new Scanner(System.in);


        String dupa = scanner.nextLine();
        System.out.println(dupa);
        dupa = scanner.nextLine();
        System.out.println(dupa);
       dupa = scanner.nextLine();
        System.out.println(dupa);


    }
}
