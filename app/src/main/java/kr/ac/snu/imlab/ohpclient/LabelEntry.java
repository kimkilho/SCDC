package kr.ac.snu.imlab.ohpclient;

/**
 * Created by kilho on 15. 6. 25.
 */
public class LabelEntry extends ProbeEntry {
  private String name;
  private boolean isLogged;

  public LabelEntry(String name, Class probeClass) {
    super(probeClass);
    this.name = name;
    this.isLogged = false;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {

    this.name = name;
  }

  public boolean isLogged() {
    return this.isLogged;
  }

  public void setLogged(boolean isLogged) {
    this.isLogged = isLogged;
  }
}
