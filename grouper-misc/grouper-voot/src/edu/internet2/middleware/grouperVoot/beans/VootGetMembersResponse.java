package edu.internet2.middleware.grouperVoot.beans;

/**
 * response to get members request
 * @author mchyzer
 *
 */
public class VootGetMembersResponse extends VootResponse {
  
  /**
   * result body
   */
  private VootPerson[] entry;

  /**
   * results
   * @return the results
   */
  public VootPerson[] getEntry() {
    return this.entry;
  }

  /**
   * results
   * @param entry1
   */
  public void setEntry(VootPerson[] entry1) {
    this.entry = entry1;
  }
  
  
}
