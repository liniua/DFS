public class Accept {

  private Integer key;
  private String value;

  // accpet process in Paxos
  public Accept(Integer key, String value) {
    super();
    this.key = key;
    this.value = value;
  }

  public void setValue(String value) {
    this.value = value;
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

  @Override
  public String toString() {
    return "Accept [key=" + key + ", value=" + value + "]";
  }
}
