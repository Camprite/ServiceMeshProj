import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class Pliki implements Runnable {
    private final Socket clientSocket;

    public Pliki(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request_string = input.readLine();
            Requests request = new Requests(request_string);
            Responses response = new Responses(request,"200");

            if (request.Type.equals("wgraj_plik")) {
                String destinationnazwaPliku = "";
                String encodedFile = "";
                try {
                    destinationnazwaPliku =  request.Line.split(";")[0];
                    encodedFile = request.Line.split(";")[1];
                } catch (Exception e){
                    response.Line = "Nie możesz wysłać pustego pliku.";
                    response.Status = "400";
                    output.println(response);
                    output.flush();
                    SerwisPlikow.toSend.add(new Requests("not_busy","1","pliki","3", SerwisPlikow.portClient));
                    return;
                }

                byte[] fileBytes = Base64.getDecoder().decode(encodedFile);

                String destinationPath = System.getProperty("user.home") + File.separator + destinationnazwaPliku;
                Files.write(Paths.get(destinationPath), fileBytes);
                response.Line = "Plik wgrany.";
            } else if (request.Type.equals("pobierz_plik")) {
                String nazwaPliku = "";
                try {
                    nazwaPliku =  request.Line.split(";")[1];
                } catch (Exception e){
                    response.Line = "Podaj nazwę pliku.";
                    response.Status = "400";
                    output.println(response);
                    output.flush();
                    SerwisPlikow.toSend.add(new Requests("not_busy","1","pliki","3", SerwisPlikow.portClient));
                    return;
                }
                String sciezkaPliku = System.getProperty("user.home") + File.separator + nazwaPliku;
                System.out.println(sciezkaPliku);
                Path path = Paths.get(sciezkaPliku);
                if (Files.exists(path)) {
                    byte[] fileBytes = Files.readAllBytes(path);
                    String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
                    response.Line = encodedFile+";"+nazwaPliku;
                } else {
                    response.Line = "Pliku nie znaleziono.";
                    response.Status = "404";
                }
            }
            output.println(response);
            output.flush();
            SerwisPlikow.toSend.add(new Requests("not_busy","1","pliki","3", SerwisPlikow.portClient));
        } catch (IOException e) {
            System.err.println("Błąd przetwarzania żądania.");
        } finally {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Błąd gniazda.");
                }
            }
        }
    }
}