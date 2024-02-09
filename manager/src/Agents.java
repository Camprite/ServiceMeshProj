import java.net.Socket;
import java.util.ArrayList;

public class Agents {
    private static ArrayList<Agent> agentsList = new ArrayList<>();

    public Agents() {

    }


    public void addAgent(Agent agent){
        this.agentsList.add(agent);
    }
    public void removeAgent(int id){
        this.agentsList.remove(id);
    }
    public Socket getSocketWithSpecificMicroservice(String serviceName){
        for (Agent agent: agentsList
             ) {
            if(agent.getSocketWithSpecificMicroservice(serviceName) != null){
                return agent.getSocketWithSpecificMicroservice(serviceName);

            }
        }
        return null;
    }
    public static Socket findSocketByAgentType(int parseInt){
        for (Agent agent: agentsList
             ) {
            if(agent.findSocketByAgentType(Integer.toString(parseInt)) != null){
                return agent.findSocketByAgentType(Integer.toString(parseInt));

            }
        }
        return null;
    }
    public String getAgentIpWithSpecificMicroservice(String microservice){
        for (Agent agent: agentsList
             ) {
            if(agent.haveMicroservice(microservice)){
                return agent.getIp();
            }
        }
        return null;
    }

    public String getAgentPortWithSpecificMicroservice(String microservice){
        for (Agent agent: agentsList
        ) {
            if(agent.haveMicroservice(microservice)){
                return agent.getPort();
            }
        }
        return null;
    }



}

