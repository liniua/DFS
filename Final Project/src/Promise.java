public class Promise {
  private boolean status;
  private Integer key;
  private String value;

  // promise process in Paxos
  public Promise(boolean status, Integer key, String value) {
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

  @Override
  public String toString() {
    return "Promise [status=" + status + ", key=" + key + ", value=" + value + "]";
  }
}
