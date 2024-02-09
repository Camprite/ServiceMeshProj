public class SharedResource {
    private int loginPort;
    private boolean loginPortAvailable = false;
    private boolean loginPortNeed = false;
    private int registerPort;
    private boolean registerPortAvailable = false;

    public synchronized int getLoginPort() {
        return this.loginPort;
    }
    public synchronized void setLoginPort(int port) {
        this.loginPort = port;
    }
    public synchronized boolean isLoginPortNeed() {
        return  loginPortNeed;
    }
    public synchronized boolean isLoginPortAvailable() {
        return  loginPortAvailable;
    }

    public synchronized void requestALoginPort() {
        this.loginPortNeed = true;
    }   public synchronized void requestALoginPortSended() {
        this.loginPortNeed = false;
    }
}
