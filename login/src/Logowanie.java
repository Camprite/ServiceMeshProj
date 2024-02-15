import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Logowanie implements Runnable {
    private final Socket clientSocket;

    public Logowanie(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestString = input.readLine();

            if (requestString.split(";").length != 2) {
                String errorMessage = "Nieprawidłowe dane uwierzytelniające.";
                output.println(errorMessage);
                output.flush();
                SerwisLogowania.notifyAgent();
                return;
            }

            String username = requestString.split(";")[0];
            String password = requestString.split(";")[1];
            try (Connection connection = PolaczenieBaza.getConnection()) {
                PreparedStatement checkUserStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                checkUserStatement.setString(1, username);
                ResultSet resultSet = checkUserStatement.executeQuery();

                if (resultSet.next()) {
                    if (resultSet.getString("password").equals(password)) {
                        String successMessage = "200: Pomyślnie zalogowano!";
                        output.println(successMessage);
                    } else {
                        String errorMessage = "Błędne hasło.";
                        output.println(errorMessage);
                    }
                } else {
                    String errorMessage = "Użytkownik nie istnieje w bazie danych.";
                    output.println(errorMessage);
                }
                output.flush();
                SerwisLogowania.notifyAgent();
            } catch (SQLException e) {
                System.err.println("Błąd.");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Błąd." + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Błąd socketa");
            }
        }
    }
}
