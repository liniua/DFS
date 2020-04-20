public class Prepare {
  private Integer key;
  private String value;

  // prepare process in Paxos
  public Prepare(Integer key, String value) {
    super();
    this.key = key;
    this.value = value;
  }
  public Integer getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setKey(Integer key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * generate next Prepare object if proposal get rejected.
   * @param value: value which won't change
   * @return
   */
  public static Prepare nextPrepare(String value) {
    return new Prepare(Number.index++, value);
  }

  @Override
  public String toString() {
    return "Perpare [key=" + key + "[value=" + value + "]";
  }
}
