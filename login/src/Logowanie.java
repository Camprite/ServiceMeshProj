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

            String request_string = input.readLine();
            Requests request = new Requests(request_string);
            Responses response = new Responses(request,"200");
            if (request.Line.split(";").length != 2) {
                response.Line = "Nieprawidłowe dane uwierzytelniające.";
                response.Status = "400";
                output.println(response);
                SerwisLogowania.outputAgent.println(new Requests("not_busy","1","login","2", SerwisLogowania.portClient));
                SerwisLogowania.outputAgent.flush();
                return;
            }

            String username = request.Line.split(";")[0];
            String password = request.Line.split(";")[1];
            try (Connection connection = PolaczenieBaza.getConnection()) {
                PreparedStatement checkUserStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                checkUserStatement.setString(1, username);
                ResultSet resultSet = checkUserStatement.executeQuery();

                if (resultSet.next()) {
                    if (resultSet.getString("password").equals(password)) {
                        response.Line = "Zalogowano!";
                    } else {
                        response.Status = "400";
                        response.Line = "Błędne hasło.";
                    }
                    output.println(response);
                } else {
                    response.Status = "400";
                    response.Line = "Użytkownik nie istnieje DB.";
                    output.println(response);
                }
                SerwisLogowania.outputAgent.println(new Requests("not_busy","1","login","2", SerwisLogowania.portClient));
                SerwisLogowania.outputAgent.flush();
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