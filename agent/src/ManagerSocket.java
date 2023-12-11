import java.net.Socket;

public class ManagerSocket {
    public Socket socket;
    public ManagerSocket(Socket socket){
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
