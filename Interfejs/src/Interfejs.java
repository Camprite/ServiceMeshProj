import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class Interfejs {
    private static String obecnyUser = "";
    private static String celnazwaPliku = "";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("""
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    Wybierz opcję!
                    """);
            if (obecnyUser.equals("")) {
                System.out.println("""
                        REJESTRACJA : Rejestracja
                        LOGOWANIE : Logowanie
                        """
                );
            }
            System.out.println("""
                    POST : Napisz coś na tablicy
                    CZYTAJ-POSTS : Wyświetl ostatnie 10 postów
                    WGRAJ : Wgraj plik na serwer
                    POBIERZ : Pobierz plik
                    WYLOGUJ : Wyloguj
                    WYJDZ : Do Widzenia
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    Twój wybór to:\s"""
            );

            String wybor = scanner.nextLine().toUpperCase();

            if (wybor.equals("WYJDZ")) {
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Koniec~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                break;
            } else if (wybor.equals("WYLOGUJ")) {
                if (obecnyUser.equals("")) {
                    System.out.println("Nie jesteś nawet zalogowany!");
                    continue;
                }
                obecnyUser = "";
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Wylogowano cię~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                continue;
            }
            String typ;
            String dane;
            String serviceName;
            String login = "";
            String haslo;
            Random rand=new Random();
            String message_id=String.valueOf(rand.nextInt(1000,10000));
            switch (wybor) {
                case "REJESTRACJA" -> {
                    if (obecnyUser.equals("")) {
                        typ = "rejestracja";
                        serviceName = "rejestracja";
                        System.out.print("Login: ");
                        login = scanner.nextLine();
                        System.out.print("Hasło: ");
                        haslo = scanner.nextLine();
                        dane = login + ";" + haslo;
                    } else {
                        System.out.println("Proszę, wybierz poprawną opcję");
                        continue;
                    }
                }
                case "LOGOWANIE" -> {
                    if (obecnyUser.equals("")) {
                        typ = "logowanie";
                        serviceName = "login";
                        System.out.print("Login: ");
                        login = scanner.nextLine();
                        System.out.print("Hasło: ");
                        haslo = scanner.nextLine();
                        dane = login + ";" + haslo;
                        message_id= String.valueOf(dane.hashCode());
                    } else {
                        System.out.println("Proszę, wybierz poprawną opcję");
                        continue;
                    }
                }
                case "POST" -> {
                    if (obecnyUser.equals("")) {
                        System.out.println("Zanim to zrobisz, proszę zaloguj się!");
                        continue;
                    }
                    typ = "post";
                    serviceName = "posty";
                    System.out.print("Treść posta: ");
                    dane = obecnyUser + ";" + scanner.nextLine();
                }
                case "CZYTAJ-POSTS" -> {
                    if (obecnyUser.equals("")) {
                        System.out.println("Zanim to zrobisz, proszę zaloguj się!");
                        continue;
                    }
                    typ = "czytaj-posts";
                    serviceName = "posty";
                    System.out.println("Ostatnie 10 postów:\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                    dane = "";
                }
                case "WGRAJ" -> {
                    if (obecnyUser.equals("")) {
                        System.out.println("Zanim to zrobisz, proszę zaloguj się!");
                        continue;
                    }
                    typ = "wgraj_plik";
                    serviceName = "pliki";
                    System.out.print("Ścieżka pliku ");
                    String sciezkaPliku = scanner.nextLine();
                    if (sciezkaPliku.equals("")) {
                        System.out.println("Zła ścieżka.");
                        continue;
                    }
                    System.out.print("Nazwa pliku: ");
                    celnazwaPliku = scanner.nextLine();
                    dane = obecnyUser + ";" + sciezkaPliku;
                }
                case "POBIERZ" -> {
                    if (obecnyUser.equals("")) {
                        System.out.println("Zanim to zrobisz, proszę zaloguj się!");
                        continue;
                    }
                    typ = "pobierz_plik";
                    serviceName = "pliki";
                    System.out.print("Nazwa pliku który chcesz pobrać: ");
                    String nazwaPliku = scanner.nextLine();
                    dane = obecnyUser + ";" + nazwaPliku;
                }
                default -> {
                    System.out.println("Proszę, wybierz poprawną opcję");
                    continue;
                }
            }

            Requests request = new Requests(typ,message_id,serviceName,"0",dane);
            String response_String = sendRequestToApiGateway(request);
            if(!response_String.contains(";")){
                System.out.println(response_String);
                continue;
            }
            Responses response = new Responses(response_String);

            System.out.println("Status odpowiedzi: "+response.Status);
            if (response.Status.equals("200")) {
                if (response.Type.equals("logowanie") || response.Type.equals("rejestracja")) {
                    obecnyUser = login;
                } else if (response.Type.equals("wgraj_plik") | response.Type.equals("pobierz_plik")) {
                    System.out.println("Przesyłanie pliku powiodło się.");
                }
            }
            if (response.Status.equals("299")) {
                String[] posts = response.Line.split(";");
                for (String post : posts) {
                    System.out.println(post);
                }
                continue;
            }
            System.out.println(response.Line);
        }
    }

    private static String sendRequestToApiGateway(Requests request) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.err.println("Plik konfiguracyjny nie załadował się.");
            return "ERROR";
        }
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));
        String apiGatewayIP = properties.getProperty("api.gateway.ip");

        if (request.Type.equals("wgraj_plik")) {
            String sciezkaPliku = request.Line.split(";")[1];
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(sciezkaPliku));
                String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
                request.Line = celnazwaPliku + ";" + encodedFile;
            } catch (IOException e) {
                System.err.println("Błąd odczytu pliku." + e.getMessage());
                return "Błąd odczytu pliku";
            }
            try (Socket socket = new Socket(apiGatewayIP, apiGatewayPort);
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                output.println(request);
                return input.readLine();
            } catch (IOException e) {
                System.err.println("Błąd połączenia z ApiGateway." + e.getMessage());
                return "Błąd połączenia z ApiGateway.";
            }
        } else if (request.Type.equals("pobierz_plik")) {
            try (Socket socket = new Socket(apiGatewayIP, apiGatewayPort);
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                output.println(request);
                String response_string = input.readLine();
                Responses response = new Responses(response_string);

                if ("200".equals(response.Status)) {
                    String encodedFile = response.Line.split(";")[0];
                    byte[] fileBytes = Base64.getDecoder().decode(encodedFile);
                    String nazwaPliku = response.Line.split(";")[1];
                    String celPath = System.getProperty("user.home") + File.separator + "DOWNLOADED_" + nazwaPliku;
                    try {
                        Files.write(Paths.get(celPath), fileBytes);
                        response.Line = "Pobranie pliku zakończone pomyślnie: " + celPath;
                        return response.toString();
                    } catch (IOException e) {
                        System.err.println("Błąd zapisu pliku" + e.getMessage());
                        return "Błąd zapisu pliku";
                    }
                } else {
                    return response.toString();
                }
            } catch (IOException e) {
                System.err.println("Błąd połączenia z ApiGateway." + e.getMessage());
                return "Błąd połączenia z ApiGateway.";
            }
        } else {
            try (Socket socket = new Socket(apiGatewayIP, apiGatewayPort);
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                output.println(request);
                return input.readLine();
            } catch (IOException e) {
                System.err.println("Błąd połączenia z ApiGateway." + e.getMessage());
                return "503;Błąd połączenia z ApiGateway.";
            }
        }
    }
}