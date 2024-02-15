import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Rejestracja implements Runnable {
    private final Socket clientSocket;

    public Rejestracja(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String requestString;
            while(true) {
                requestString = input.readLine();
                if (requestString != null) {

                    String[] requestParts = requestString.split(";");

                    if (requestParts.length != 2) {
                        output.println("Nieprawidłowe dane uwierzytelniające.");
                        continue; // Przejdź do kolejnej iteracji pętli
                    }

                    String username = requestParts[0];
                    String password = requestParts[1];

                    try (Connection connection = PolaczenieBaza.getConnection()) {
                        PreparedStatement checkUserStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                        checkUserStatement.setString(1, username);
                        ResultSet resultSet = checkUserStatement.executeQuery();

                        if (resultSet.next()) {
                            output.println("Użytkownik istnieje w bazie danych.");
                            continue; // Przejdź do kolejnej iteracji pętli
                        }

                        PreparedStatement insertUserStatement = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                        insertUserStatement.setString(1, username);
                        insertUserStatement.setString(2, password);
                        insertUserStatement.executeUpdate();

                    output.write("200: Pomyślnie zarejestrowano");
                    SerwisRejestracji.notifyAgent();
                    break;
                     }
                }

            }
            } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        }
       }


