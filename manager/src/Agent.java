import java.net.Socket;
import java.util.ArrayList;

public class Agent{
    ArrayList<String> microservices;
    String port;
    String ip;
    String agentType;
    Socket socket;

    public String getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public Socket getSocketWithSpecificMicroservice(String serviceName){
        for (String microservice : microservices
             ) {
            if(microservice.equals(serviceName)){
                return this.socket;
            }
        }
        return null;
    }
    public Socket findSocketByAgentType(String agentType){
        if(this.agentType.equals(agentType)){
            return this.socket;
        }
        return null;
    }
    public Agent(String ip, String port, String agentType , ArrayList microservices, Socket socket) {
        this.ip = ip;
        this.port= port;
        this.agentType= agentType;
        this.microservices = microservices;
        this.socket = socket;
    }
    public boolean haveMicroservice(String microservice){
        for (String service: microservices
             ) {
            if(microservice.equals(service)){
                return true;
            }
        }
        return false;
    }
    public boolean haveMicroserviceOpened(String microservice){
        for (String service: microservices
        ) {
            if(microservice.equals(service)){
                return true;
            }
        }
        return false;
    }
}