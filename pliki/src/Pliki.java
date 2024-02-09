import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class Pliki implements Runnable {
    private final Socket clientSocket;

    public Pliki(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request_string = input.readLine();

            if (request_string == null) {
                return;
            }

            String[] requestParts = request_string.split(";");
            String requestType = requestParts[0];

            if (requestType.equals("wgraj_plik")) {
                try {
                    String destinationnazwaPliku = requestParts[1];
                    String encodedFile = requestParts[2];
                    byte[] fileBytes = Base64.getDecoder().decode(encodedFile);
                    String destinationPath = System.getProperty("user.home") + File.separator + destinationnazwaPliku;
                    Files.write(Paths.get(destinationPath), fileBytes);
                    output.println("Plik wgrany.");
                } catch (Exception e) {
                    output.println("Błąd podczas wgrywania pliku: " + e.getMessage());
                }
            } else if (requestType.equals("pobierz_plik")) {
                try {
                    String nazwaPliku = requestParts[1];
                    String sciezkaPliku = System.getProperty("user.home") + File.separator + nazwaPliku;
                    Path path = Paths.get(sciezkaPliku);
                    if (Files.exists(path)) {
                        byte[] fileBytes = Files.readAllBytes(path);
                        String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
                        output.println(encodedFile + ";" + nazwaPliku);
                    } else {
                        output.println("Pliku nie znaleziono.");
                    }
                } catch (Exception e) {
                    output.println("Błąd podczas pobierania pliku: " + e.getMessage());
                }
            } else {
                output.println("Nieprawidłowe żądanie.");
            }
            SerwisPlikow.notifyAgent();
        } catch (IOException e) {
            System.err.println("Błąd przetwarzania żądania: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Błąd zamykania gniazda: " + e.getMessage());
            }
        }
    }
}
