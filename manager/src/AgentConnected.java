import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


    public class AgentConnected implements Runnable {
        private final Socket agentSocket;
        private final String requestFromManager;

        public AgentConnected(Socket clientSocket, String requestFromManager) {
            this.agentSocket = clientSocket;
            this.requestFromManager = requestFromManager;
        }

        @Override
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
                 PrintWriter output = new PrintWriter(agentSocket.getOutputStream(), true)) {

                output.write(requestFromManager);

                String request = input.readLine();
                String[] agentData = request.split(";");

                if (agentData.length != 3) {
                    output.println("Bad agent data.");
                    return;
                }

                String agentProcId = agentData[1];
                String agentRequest = agentData[2];
                System.out.println(agentData);

            } catch (IOException e) {
                System.err.println("Błąd." + e.getMessage());
            } finally {
                try {
                    agentSocket.close();
                } catch (IOException e) {
                    System.err.println("Błąd socketa");
                }
            }
        }
    }

