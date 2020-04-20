public class Announce {

  private boolean status;
  private Integer key;
  private String value;

  // announce process in Paxos
  public Announce(boolean status, Integer key, String value) {
    super();
    this.status = status;
    this.key = key;
    this.value = value;
  }

  public boolean isStatus() {
    return status;
  }
  public void setStatus(boolean status) {
    this.status = status;
  }
  public Integer getKey() {
    return key;
  }
  public void setKey(Integer key) {
    this.key = key;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
}
