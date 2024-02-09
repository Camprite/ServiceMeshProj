import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Posty implements Runnable {
    private final Socket clientSocket;

    public Posty(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestString = input.readLine();
            String[] requestParts = requestString.split(";");

            if (requestParts.length < 2) {
                output.println("Nieprawidłowe żądanie.");
                return;
            }

            String requestType = requestParts[0];

            if (requestType.equals("post")) {
                handlePostRequest(requestParts, output);
            } else if (requestType.equals("czytaj-posts")) {
                handleReadPostsRequest(output);
            } else {
                output.println("Nieprawidłowe żądanie.");
            }
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

    private void handlePostRequest(String[] requestParts, PrintWriter output) {
        String username = requestParts[1];
        String postData = requestParts[2];

        try (Connection connection = PolaczenieBaza.getConnection()) {
            PreparedStatement getUserIdStatement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
            getUserIdStatement.setString(1, username);
            ResultSet resultSet = getUserIdStatement.executeQuery();

            if (!resultSet.next()) {
                output.println("Użytkownik nie istnieje w bazie danych.");
                return;
            }

            int userId = resultSet.getInt("id");

            PreparedStatement insertPostStatement = connection.prepareStatement("INSERT INTO posts (user, content) VALUES (?, ?)");
            insertPostStatement.setInt(1, userId);
            insertPostStatement.setString(2, postData);
            insertPostStatement.executeUpdate();
            output.println("Post pomyślnie dodany.");
            SerwisPostow.notifyAgent();
        } catch (SQLException e) {
            System.err.println("Błąd przetwarzania żądania: " + e.getMessage());
            output.println("Błąd przetwarzania żądania.");
        }
    }

    private void handleReadPostsRequest(PrintWriter output) {
        try (Connection connection = PolaczenieBaza.getConnection()) {
            PreparedStatement returnPostStatement = connection.prepareStatement("SELECT * FROM posts ORDER BY ID DESC LIMIT 10");
            ResultSet resultSet = returnPostStatement.executeQuery();

            StringBuilder posts = new StringBuilder();
            while (resultSet.next()) {
                String content = resultSet.getString("content");
                int userId = resultSet.getInt("user");
                String timestamp = resultSet.getString("tstamp");

                PreparedStatement getUsernameStatement = connection.prepareStatement("SELECT username FROM users WHERE id = ?");
                getUsernameStatement.setInt(1, userId);
                ResultSet userResultSet = getUsernameStatement.executeQuery();

                if (userResultSet.next()) {
                    String username = userResultSet.getString("username");
                    posts.append("Użytkownik ").append(username).append("\tNapisał: ").append(content).append("\tDodano: ").append(timestamp).append(";");
                }
            }
            output.println(posts.toString());
            SerwisPostow.notifyAgent();
        } catch (SQLException e) {
            System.err.println("Błąd przetwarzania żądania: " + e.getMessage());
            output.println("Błąd przetwarzania żądania.");
        }
    }
}
