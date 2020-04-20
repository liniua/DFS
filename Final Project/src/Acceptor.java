import java.io.Serializable;

public class Acceptor implements Serializable {
  private int port;
  private Integer maxKey;
  private String maxValue;
  private Integer acceptKey;
  private String acceptValue;

  public Acceptor(int port) {
    this.port = port;
    this.maxKey = null;
    this.maxValue = null;
    this.acceptKey = null;
    this.acceptValue = null;
  }

  public boolean failure() {
    //simulate 30% failure possibility
    if(Math.random()<0.3) {
      System.out.println("Acceptor "+port+" failed");
      return true;
    }
    return false;
  }

  public static Promise doPromise(Acceptor acceptor, Prepare perpare)  {
    // if the accepter maxKey is null.
    if(acceptor.getMaxKey() == null) {
      acceptor.setMaxKey(perpare.getKey());
      acceptor.setMaxValue(perpare.getValue());
      System.out.println("perpare: "+acceptor+perpare+" ok");
      return new Promise(true, perpare.getKey(), perpare.getValue());
    }
    // if the acceptor gets a larger maxKey, update and make promise.
    if(acceptor.getMaxKey()<perpare.getKey()) {
      Integer k = acceptor.getMaxKey();
      String v = acceptor.getMaxValue();
      acceptor.setMaxKey(perpare.getKey());
      acceptor.setMaxValue(perpare.getValue());
      System.out.println("perpare: "+acceptor+perpare+" ok");
      return new Promise(true, k, v);
    }
    // acceptor has larger maxKey, return not promise
    System.out.println("perpare: "+acceptor+perpare+" error");
    return new Promise(false, null, null);
  }

  // anounce the results back to Proposer
  public static Announce doAnnounce(Acceptor acceptor, Accept accept) {
    if(acceptor.getMaxKey() == null) {
      acceptor.setAcceptKey(accept.getKey());
      acceptor.setAcceptValue(accept.getValue());
      System.out.println("accept: "+acceptor+accept+" ok");
      return new Announce(true, acceptor.getAcceptKey(), acceptor.getAcceptValue());
    }
    if(acceptor.getMaxKey() <= accept.getKey()) {

      acceptor.setMaxKey(accept.getKey());
      acceptor.setMaxValue(accept.getValue());
      acceptor.setAcceptKey(accept.getKey());
      acceptor.setAcceptValue(accept.getValue());
      Integer k = acceptor.getAcceptKey();
      String v = acceptor.getAcceptValue();
      System.out.println("accept: "+acceptor+accept+" ok");
      return new Announce(true, k, v);
    }
    System.out.println("accept: "+acceptor+accept+" error");
    return new Announce(false, null, null);
  }

//  public int getPort() {
//    return port;
//  }

  public Integer getMaxKey() {
    return maxKey;
  }

  public void setMaxKey(Integer maxKey) {
    this.maxKey = maxKey;
  }

  public String getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  public Integer getAcceptKey() {
    return acceptKey;
  }

  public void setAcceptKey(Integer acceptKey) {
    this.acceptKey = acceptKey;
  }

  public String getAcceptValue() {
    return acceptValue;
  }

  public void setAcceptValue(String acceptValue) {
    this.acceptValue = acceptValue;
  }

  @Override
  public String toString() {
    return "Acceptor [port=" + port + ", maxKey=" + maxKey + ", acceptKey=" + acceptKey + "]";
  }
}
