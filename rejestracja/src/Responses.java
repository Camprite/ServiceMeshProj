public class Responses {
    public String Type,Message_id,Status,Service_name,Agent_type,Line;

    public Responses(String response){
        String[] split_request = response.split(";");
        Type = split_request[0].split(":")[1];
        Message_id = split_request[1].split(":")[1];
        Status = split_request[2].split(":")[1];
        Service_name = split_request[3].split(":")[1];
        if(split_request[4].split(":").length > 1){
            Agent_type = split_request[4].split(":")[1];
        } else {
            Agent_type = "";
        }
        if(split_request[5].split(":").length > 1){
            StringBuilder build_line = new StringBuilder(split_request[5].split(":")[1]);
            for (int i=2; i<split_request[5].split(":").length;i++ ){
                build_line.append(":").append(split_request[5].split(":")[i]);
            }
            for (int i=6; i<split_request.length;i++){
                build_line.append(";");
                build_line.append(split_request[i]);
            }
            Line = build_line.toString();
        } else {
            Line = "";
        }
    }

    public Responses(Requests request,String Status){
        this.Type = request.Type;
        this.Message_id = request.Message_id;
        this.Status = Status;
        this.Service_name = request.Service_name;
        this.Agent_type = request.Agent_type;
        this.Line = request.Line;
    }

    public Responses(String Type,String Message_id,String Status,String Service_name,String Agent_type,String Line){
        this.Type = Type;
        this.Message_id = Message_id;
        this.Status = Status;
        this.Service_name = Service_name;
        this.Agent_type = Agent_type;
        this.Line = Line;
    }

    public String toString(){
        return "Type:"+Type+";Message_id:"+Message_id+";Status:"+Status+";Service_name:"+Service_name+";Agent_type:"+Agent_type+";Line:"+Line;
    }
}
