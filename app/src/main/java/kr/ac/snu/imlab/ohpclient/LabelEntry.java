package kr.ac.snu.imlab.ohpclient;

/**
 * Created by kilho on 15. 6. 25.
 */
public class LabelEntry {
  private String name;
  private boolean isEnabled;

  public LabelEntry(String name) {
    this.name = name;
    this.isEnabled = false;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isEnabled() {
    return this.isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }
}
