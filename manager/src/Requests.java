import java.util.ArrayList;
import java.util.Arrays;

public class Requests {
    public String Type,Message_id,Service_name,Line;
    public String Agent_type;

    public Requests(String request){
        String[] split_request = request.split(";");
        Type = split_request[0].split(":")[1];
        Message_id = split_request[1].split(":")[1];
        Service_name = split_request[2].split(":")[1];
        if(split_request[3].split(":").length > 1){
            StringBuilder build_line = new StringBuilder(split_request[3].split(":")[1]);
            for (int i=2; i<split_request[4].split(":").length;i++ ){
                build_line.append(":").append(split_request[3].split(":")[i]);
            }
            for (int i=5; i<split_request.length;i++){
                build_line.append(";");
                build_line.append(split_request[i]);
            }
            Line = build_line.toString();
        } else {
            Line = "";
        }
    }

    public Requests(Responses request){
        this.Type = request.Type;
        this.Message_id = request.Message_id;
        this.Service_name = request.Service_name;
        this.Line = request.Line;
    }

    public Requests(String Type,String Message_id,String Service_name,String Line){
        this.Type = Type;
        this.Message_id = Message_id;
        this.Service_name = Service_name;
        this.Line = Line;
    }
    public String getIpFromInitialRequest(){
        return this.Line.split(";")[0];
    }
    public String getPortFromInitialRequest(){
        return this.Line.split(";")[1];
    }
    public ArrayList<String> getListOfMicroservices(){
        ArrayList<String> lista = new ArrayList<String>(Arrays.asList(this.Line.split(";")[2].split(" ")));
        return  lista;
    }
    public String toString(){
        return "Type:"+Type+";Message_id:"+Message_id+";Service_name:"+Service_name+";Line:"+Line;
    }
}
