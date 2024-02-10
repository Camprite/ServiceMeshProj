import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SerwisRejestracji {
    public static void main(String[] args) {
        // Pobierz argumenty z wiersza poleceń
        if (args.length < 4) {
            System.out.println("Nieprawidłowa liczba argumentów.");
            System.out.println("Usage: java SerwisRejestracji <ipAgent> <portAgent> <port> <ip>");
            return;
        }

        String ipAgent = args[0];
        int portAgent = Integer.parseInt(args[1]);
        int port = Integer.parseInt(args[2]);
        String ip = args[3];

        try {
            // Nawiąż połączenie z ApiGateway
            try (Socket socket = new Socket(ip, port)) {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                // Wyślij wiadomość do ApiGateway
                output.println("Udana rejestracja!");
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas komunikacji z ApiGateway: " + e.getMessage());
        }
    }
}
