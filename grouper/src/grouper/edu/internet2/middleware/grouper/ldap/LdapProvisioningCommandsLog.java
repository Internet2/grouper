package edu.internet2.middleware.grouper.ldap;

public class LdapProvisioningCommandsLog {

  private StringBuilder log = new StringBuilder();

  private boolean disabled = false;

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public StringBuilder getLog() {
    return log;
  }


  public void setLog(StringBuilder log) {
    this.log = log;
  }

  /**
   * append to the log in a thread-safe way, respecting the max size
   * @param text
   * @param maxLogSize
   */
  public synchronized void appendIfRoom(CharSequence text, long maxLogSize) {
    if (this.disabled) {
      return;
    }
    if (this.log.length() + text.length() < maxLogSize) {
      this.log.append(text);
    }
  }

  public LdapProvisioningCommandsLog() {
  }

}
