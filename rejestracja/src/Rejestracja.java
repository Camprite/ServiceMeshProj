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

            String request_string = input.readLine();
            Requests request = new Requests(request_string);
            Responses response = new Responses(request,"200");

            if (request.Line.split(";").length != 2) {
                response.Line = "Nieprawidłowe dane uwierzytelniające.";
                response.Status = "406";
                output.println(response);
                SerwisRejestracji.toSend.add(new Requests("not_busy","1","rejestracja","2", SerwisRejestracji.portClient));
                return;
            }

            String username = request.Line.split(";")[0];
            String password = request.Line.split(";")[1];

            try (Connection connection = PolaczenieBaza.getConnection()) {
                PreparedStatement checkUserStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                checkUserStatement.setString(1, username);
                ResultSet resultSet = checkUserStatement.executeQuery();

                if (resultSet.next()) {
                    response.Line = "Użytkownik istnieje w bazie danych.";
                    response.Status = "409";
                    output.println(response);
                    output.flush();
                    SerwisRejestracji.toSend.add(new Requests("not_busy","1","rejestracja","2", SerwisRejestracji.portClient));
                    return;
                }

                PreparedStatement insertUserStatement = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                insertUserStatement.setString(1, username);
                insertUserStatement.setString(2, password);
                insertUserStatement.executeUpdate();

                response.Line = "Pomyślnie zarejestrowano. Gratuluję.";
                output.println(response);
                output.flush();
                SerwisRejestracji.toSend.add(new Requests("not_busy","1","rejestracja","2", SerwisRejestracji.portClient));
            } catch (SQLException e) {
                System.err.println("Błąd");
            }
        } catch (IOException e) {
            System.err.println("Błąd");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Błąd" + e.getMessage());
            }
        }
    }
}