import java.util.ArrayList;

public class test {
    public static ArrayList<ArrayList> AgentNumberResponsible = new ArrayList<>();
    public static void main(String[] args) {
        ArrayList<String> AgentResponsible = new ArrayList<>();
        String microservice = "api";
        AgentResponsible.add(microservice);
        AgentNumberResponsible.add(AgentResponsible); //Adding responsible to Agent with 0 index

        AgentResponsible = new ArrayList<>();
        AgentResponsible.add("login");
        AgentResponsible.add("rejestracja");
        AgentNumberResponsible.add(AgentResponsible); //Adding responsible to Agent with 1 index

        AgentResponsible = new ArrayList<>();
        AgentResponsible.add("posty");
        AgentResponsible.add("pliki");
        AgentNumberResponsible.add(AgentResponsible); //Adding responsible to Agent with 2 index
        System.out.println(AgentResponsible.toString());
////        System.out.println(AgentNumberResponsible);
//        for (ArrayList<String> agentMicroservices: AgentNumberResponsible
//        ) {
//            System.out.println("");
//          for(String s : agentMicroservices){
//              System.out.println(s);
//          }



//        }
    }
}
