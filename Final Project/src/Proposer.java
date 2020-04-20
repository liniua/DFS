public class Proposer {

  private int port;

  public Proposer(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return "Proposer [port=" + port + "]";
  }
}
