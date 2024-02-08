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

            String request_string = input.readLine();
            Requests request = new Requests(request_string);
            Responses response = new Responses(request,"200");

            if (request.Type.equals("post")) {

                try (Connection connection = PolaczenieBaza.getConnection()) {
                    String username = "";
                    String postData = "";
                    try {
                        username = request.Line.split(";")[0];
                        postData=request.Line.split(";")[1];
                    } catch (Exception e){
                        response.Line = "Nie możesz wysłać pustego postu.";
                        response.Status = "400";
                        output.println(response);
                        output.flush();
                        SerwisPostow.toSend.add(new Requests("not_busy","1","posty","3", SerwisPostow.portClient));
                        return;
                    }
                    PreparedStatement getUserIdStatement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
                    getUserIdStatement.setString(1, username);
                    ResultSet resultSet = getUserIdStatement.executeQuery();

                    if (!resultSet.next()) {
                        response.Line = "Użytkownik nie istnieje w DB.";
                        response.Status = "400";
                        output.println(response);
                        output.flush();
                        SerwisPostow.toSend.add(new Requests("not_busy","1","posty","3", SerwisPostow.portClient));
                        return;
                    }
                    int userId = resultSet.getInt("id");

                    PreparedStatement insertPostStatement = connection.prepareStatement("INSERT INTO posts (user, content) VALUES (?, ?)");
                    insertPostStatement.setInt(1, userId);
                    insertPostStatement.setString(2, postData);
                    insertPostStatement.executeUpdate();
                    response.Line = "Post Pomyślnie dodany.";
                } catch (SQLException e) {
                    System.err.println("Błąd");
                }
            } else if (request.Type.equals("czytaj-posts")) {
                try (Connection connection = PolaczenieBaza.getConnection()) {
                    PreparedStatement returnPostStatement = connection.prepareStatement("SELECT * FROM posts ORDER BY ID DESC LIMIT 10");
                    ResultSet resultSet = returnPostStatement.executeQuery();

                    StringBuilder posts = new StringBuilder();
                    while (resultSet.next()) {
                        String content = resultSet.getString("content");
                        int userId = resultSet.getInt("user");
                        String tstamp = resultSet.getString("tstamp");

                        PreparedStatement getUsernameStatement = connection.prepareStatement("SELECT username FROM users WHERE id = ?");
                        getUsernameStatement.setInt(1, userId);
                        ResultSet userResultSet = getUsernameStatement.executeQuery();

                        if (userResultSet.next()) {
                            String uname = userResultSet.getString("username");
                            posts.append("Uzytkownik ").append(uname).append("\s\sNapisał:\t").append(content).append("\t%\tDodano: ").append(tstamp).append(";");
                        }
                    }
                    response.Line = posts.toString();
                    response.Status = "299";

                } catch (SQLException e) {
                    System.err.println("Błąd");
                }
            }
            output.println(response);
            output.flush();
            SerwisPostow.toSend.add(new Requests("not_busy","1","posty","3", SerwisPostow.portClient));
        } catch (IOException e) {
            System.err.println("Błąd");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Błąd");
            }
        }
    }
}